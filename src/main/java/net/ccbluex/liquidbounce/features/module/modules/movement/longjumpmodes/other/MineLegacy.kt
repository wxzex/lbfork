/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.other

import net.ccbluex.liquidbounce.LiquidBounce

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.server.S08PacketPlayerPosLook

object MineLegacy : LongJumpMode("MineLegacy") {
    private var isFlag = false

    fun onEnable() {
        isFlag = false
    }

    override fun onUpdate() {
        if(mc.thePlayer.onGround){
            if(MovementUtils.isMoving){
                mc.thePlayer.jump()
            }
        }else if(MovementUtils.isMoving){
            mc.thePlayer.motionY = 0.42
            MovementUtils.strafe(1.97F)
        }

    }

    fun onPacket(event: PacketEvent) {
        if (event.packet is S08PacketPlayerPosLook) {
            TODO("Autodisable")
        }
    }
}