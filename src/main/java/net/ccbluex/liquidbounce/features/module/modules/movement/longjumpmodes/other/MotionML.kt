/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import kotlin.math.sin
import kotlin.math.cos

object MotionML : LongJumpMode("MotionML") {
    fun toRadians(degrees: Double): Double {
        return degrees * Math.PI / 180
    }
    var yaw: Double = toRadians(mc.thePlayer.rotationYaw.toDouble())
    override fun onUpdate() {
        mc.thePlayer.motionX = 1.97 * -sin(yaw)
        mc.thePlayer.motionZ = 1.97 * cos(yaw)
        mc.thePlayer.motionY = 0.42
    }
}