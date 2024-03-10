/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode

object MotionML : LongJumpMode("MotionML") {
    fun radians(degrees: Double): Double {
        return degrees * Math.PI / 180
    }
    var rotationYaw: Float = player.yaw
    yaw = radians(rotationYaw.toDouble())
    override fun onUpdate() {
        mc.thePlayer.motionY = 0.42
        mc.thePlayer.motionX = 1.97 * -sin(yaw)).toInt()
        mc.thePlayer.motionZ = 1.97 * cos(yaw)).toInt()
    }
}