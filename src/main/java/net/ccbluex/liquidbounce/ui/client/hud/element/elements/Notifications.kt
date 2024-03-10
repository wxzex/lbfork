/**
 * SpaceKing OpenSource Free Share
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.render.HUD
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.pow

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {
    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        val notifications = mutableListOf<Notification>()
        //FUCK YOU java.util.ConcurrentModificationException
        for ((index, notify) in LiquidBounce.hud.notifications.withIndex()) {
            GL11.glPushMatrix()

            if (notify.drawNotification(index)) {
                notifications.add(notify)
            }

            GL11.glPopMatrix()
        }
        for (notify in notifications) {
            LiquidBounce.hud.notifications.remove(notify)
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification))
                LiquidBounce.hud.addNotification(exampleNotification)

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
//            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-exampleNotification.width.toFloat() + 80, -exampleNotification.height.toFloat()-24.5f, 80F, -24.5F)
        }

        return null
    }

}

class Notification(val title: String, val content: String, val type: NotifyType, val time: Int = 2000, val animeTime: Int = 500) {
    val height = 30
    var fadeState = FadeState.IN
    var nowY = -height
    var string = ""
    var displayTime = System.currentTimeMillis()
    var animeXTime = System.currentTimeMillis()
    var animeYTime = System.currentTimeMillis()
    val width = Fonts.font32.getStringWidth(content) + 53

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(2f)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            var c = RenderUtils.getGradientOffset(Color(HUD.r.get(),HUD.g.get(),HUD.b.get()), Color(HUD.r2.get(),HUD.g2.get(),HUD.b2.get(), 1), (Math.abs(System.currentTimeMillis() / 360.0 + (i* 34 / 360) * 56 / 100) / 10)).rgb
            val f2 = (c shr 24 and 255).toFloat() / 255.0f
            val f22 = (c shr 16 and 255).toFloat() / 255.0f
            val f3 = (c shr 8 and 255).toFloat() / 255.0f
            val f4 = (c and 255).toFloat() / 255.0f
            GlStateManager.color(f22, f3, f4, f2)
            GL11.glVertex2f(
                (x + Math.cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                (y + Math.sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
            )
            i -= 360f / 90.0f
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }
    /**
     * Draw notification
     */
    /**
     * SpaceKing OpenSource Free Share
     */
    fun drawNotification(index: Int): Boolean {
        val realY = -(index + 1) * (height + 10)
        val nowTime = System.currentTimeMillis()
        //Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct > 1) {
                nowY = realY
                pct = 1.0
            } else {
                pct = easeOutBack(pct)
            }
            GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
        } else {
            animeYTime = nowTime
        }
        GL11.glTranslated(0.0, nowY.toDouble(), 0.0)

        //X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            FadeState.IN -> {
                if (pct > 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = easeOutBack(pct)
            }

            FadeState.STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime) > time) {
                    fadeState = FadeState.OUT
                    animeXTime = nowTime
                }
            }

            FadeState.OUT -> {
                if (pct > 1) {
                    fadeState = FadeState.END
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = 1 - easeInBack(pct)
            }

            FadeState.END -> {
                return true
            }
        }
        if (type.toString() == "SUCCESS") {
            string = "a"
        }
        if (type.toString() == "ERROR") {
            string = "B"
        }
        if (type.toString() == "WARNING") {
            string = "D"
        }
        if (type.toString() == "INFO") {
            string = "C"
        }
        GL11.glScaled(pct,pct,pct)
        GL11.glTranslatef(-width.toFloat()/2 , -height.toFloat()/2, 0F)
        RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), Color(63, 63, 63, 140))
        drawGradientSideways(0.0, height - 1.7,
            (width * ((nowTime - displayTime) / (animeTime * 2F + time))).toDouble(), height.toDouble(), Color(HUD.r.get(),HUD.g.get(),HUD.b.get()).rgb, Color(HUD.r2.get(),HUD.g2.get(),HUD.b2.get()).rgb)
        Fonts.font37.drawStringWithShadow("$title", 24.5F, 7F, Color.WHITE.rgb)
        Fonts.font32.drawStringWithShadow("$content" + " (" + BigDecimal(((time - time * ((nowTime - displayTime) / (animeTime * 2F + time))) / 1000).toDouble()).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "s)", 24.5F, 17.3F, Color.WHITE.rgb)
        drawFilledCircle(13, 15, 8.5F,Color.BLACK)
        Fonts.Nicon80.drawString(string, 3, 8, Color.WHITE.rgb)
        drawCircle(12.9f,15.0f,8.8f, 0,360)
        GlStateManager.resetColor()


        return false
    }

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f1 = (col1 shr 16 and 0xFF) / 255.0f
        val f2 = (col1 shr 8 and 0xFF) / 255.0f
        val f3 = (col1 and 0xFF) / 255.0f
        val f4 = (col2 shr 24 and 0xFF) / 255.0f
        val f5 = (col2 shr 16 and 0xFF) / 255.0f
        val f6 = (col2 shr 8 and 0xFF) / 255.0f
        val f7 = (col2 and 0xFF) / 255.0f
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        GL11.glPushMatrix()
        GL11.glBegin(7)
        GL11.glColor4f(f1, f2, f3, f)
        GL11.glVertex2d(left, top)
        GL11.glVertex2d(left, bottom)
        GL11.glColor4f(f5, f6, f7, f4)
        GL11.glVertex2d(right, bottom)
        GL11.glVertex2d(right, top)
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
    }

    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GLUtils.glEnable(GL11.GL_BLEND)
        GLUtils.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GLUtils.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * Math.sin(i * dAngle)).toFloat()
            y = (radius * Math.cos(i * dAngle)).toFloat()
            GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            GL11.glVertex2f(xx + x, yy + y)
        }
        GlStateManager.color(0f, 0f, 0f)
        GL11.glEnd()
        GL11.glPopAttrib()
    }

    fun easeInBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return c3 * x * x * x - c1 * x * x
    }

    fun easeOutBack(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1

        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }
}

enum class NotifyType() {
    SUCCESS(),
    ERROR(),
    WARNING(),
    INFO();
}


enum class FadeState { IN, STAY, OUT, END }
/**
 * SpaceKing OpenSource Free Share
 */


