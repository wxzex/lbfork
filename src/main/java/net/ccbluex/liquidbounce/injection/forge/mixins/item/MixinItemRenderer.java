/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.item;

import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.ccbluex.liquidbounce.features.module.modules.render.Animation;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.features.module.modules.render.AntiBlind;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.renderer.GlStateManager.*;

@Mixin(ItemRenderer.class)
@SideOnly(Side.CLIENT)
public abstract class MixinItemRenderer {

    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private float equippedProgress;

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private ItemStack itemToRender;

    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks);

    @Shadow
    protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow
    protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Shadow
    protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow
    protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void doItemUsedTransformations(float swingProgress);

    //@Shadow
    //public abstract void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform);

    @Shadow
    private final RenderItem itemRenderer;

    @Shadow
    private boolean isBlockTranslucent(Block blockIn) {
        return false;
    }

    public MixinItemRenderer(Minecraft mcIn) {
        this.itemRenderer = mcIn.getRenderItem();
    }

    @Shadow
    protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {
        if (heldStack != null) {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();
            if (this.itemRenderer.shouldRenderItemIn3D(heldStack)) {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                if (this.isBlockTranslucent(block)) {
                    GlStateManager.depthMask(false);
                }
            } else {
                if(Animations.INSTANCE.getState()){
                    double factor = 1 - Animations.getDownScale();
                    GlStateManager.scale(factor, factor, factor);
                }
            }

            this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);
            if (this.isBlockTranslucent(block)) {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }

    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void renderItemInFirstPerson(float partialTicks) {
        final Animations animations = Animations.INSTANCE;
        final Animation animation;
        float f = 1f - (prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks);
        EntityPlayerSP abstractclientplayer = mc.thePlayer;
        float f1 = abstractclientplayer.getSwingProgress(partialTicks);
        float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        float swingProgress = Animations.INSTANCE.getState() && Animations.INSTANCE.getOnepointseven() ?
                abstractclientplayer.getSwingProgress(partialTicks) : 0f;
        if(Animations.INSTANCE.getState()) {
            GL11.glTranslated(Animations.getItemPosX(), Animations.getItemPosY(), Animations.getItemPosZ());
            if(animations.getBetterBobbing())
                GlStateManager.scale(10, 10, 10);
        }
        rotateArroundXAndY(f2, f3);
        setLightMapFromPlayer(abstractclientplayer);
        rotateWithPlayerRotations(abstractclientplayer, partialTicks);
        enableRescaleNormal();
        pushMatrix();

        if (itemToRender != null) {
            boolean isForceBlocking = (itemToRender.getItem() instanceof ItemSword && KillAura.INSTANCE.getRenderBlocking()) || NoSlow.INSTANCE.isUNCPBlocking();

            if (Animations.INSTANCE.getState() && Animations.INSTANCE.getOnepointseven() && (itemToRender.getItem() instanceof ItemFishingRod || itemToRender.getItem() instanceof ItemCarrotOnAStick)) {
                GlStateManager.translate(0.08F, -0.027F, -0.33F);
                GlStateManager.scale(0.93F, 1.0F, 1.0F);
            }

            if (itemToRender.getItem() instanceof ItemMap) {
                renderItemMap(abstractclientplayer, f2, f, f1);
            } else if (abstractclientplayer.getItemInUseCount() > 0 || isForceBlocking) {
                EnumAction enumaction = isForceBlocking ? EnumAction.BLOCK : itemToRender.getItemUseAction();

                switch (enumaction) {
                    case NONE:
                        transformFirstPersonItem(f, 0f);
                        break;
                    case EAT:
                    case DRINK:
                        performDrinking(abstractclientplayer, partialTicks);
                        transformFirstPersonItem(f, swingProgress);
                        break;
                    case BLOCK:
                        if (animations.handleEvents()) {
                            animation = animations.getAnimation();
                        } else { // Use 1.7 animation
                            animation = animations.getDefaultAnimation();
                        }

                        if (animation != null) {
                            animation.transform(f1, f, abstractclientplayer);
                        }
                        break;
                    case BOW:
                        transformFirstPersonItem(f, swingProgress);
                        doBowTransformations(partialTicks, abstractclientplayer);
                }
            } else {

                if (!animations.handleEvents() || !animations.getOddSwing()) {
                    doItemUsedTransformations(f1);
                }

                transformFirstPersonItem(f, f1);
            }

            if (Animations.INSTANCE.getState() && Animations.INSTANCE.getOnepointseven() && !itemRenderer.shouldRenderItemIn3D(itemToRender)) {
                liquidBounce$onePointSevenItem();
            }
            renderItem(abstractclientplayer, itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        } else if (!abstractclientplayer.isInvisible()) {
            renderPlayerArm(abstractclientplayer, f, f1);
        }

        popMatrix();
        disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    @Inject(method = "doBowTransformations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V"))
    private void preBowTransform(float partialTicks, AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (Animations.INSTANCE.getState() && Animations.INSTANCE.getOnepointseven()) {
            GlStateManager.rotate(-335.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.5F, 0.0F);
        }
    }

    @Inject(method = "doBowTransformations", at = @At(value = "TAIL"))
    private void postBowTransform(float partialTicks, AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (Animations.INSTANCE.getState() && Animations.INSTANCE.getOnepointseven()) {
            GlStateManager.translate(0.0F, -0.5F, 0.0F);
            GlStateManager.rotate(50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(335.0F, 0.0F, 0.0F, 1.0F);
        }
    }


    @Redirect(method = "renderFireInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    private void renderFireInFirstPerson(float p_color_0_, float p_color_1_, float p_color_2_, float p_color_3_) {
        final AntiBlind antiBlind = AntiBlind.INSTANCE;
        if (p_color_3_ != 1F && antiBlind.handleEvents()) {
            GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, antiBlind.getFireEffect());
        } else {
            GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, p_color_3_);
        }
    }

    @Unique
    private static void liquidBounce$onePointSevenItem() {
        float scale = 1.5F / 1.7F;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(5.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.29F, 0.149F, -0.0328F);
    }
}
