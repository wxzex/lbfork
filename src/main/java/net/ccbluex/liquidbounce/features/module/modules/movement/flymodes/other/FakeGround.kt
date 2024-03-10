package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

object FakeGround : FlyMode("FakeGround") {
    var flyy = 0.0
    override fun onEnable() {
        flyy = mc.thePlayer.posY
    }
    override fun onBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= flyy) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, flyy, event.z + 1.0)
        }
    }
}
