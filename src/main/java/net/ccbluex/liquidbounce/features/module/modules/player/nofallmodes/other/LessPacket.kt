package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

object LessPacket : NoFallMode("LessPacket") {
    override fun onUpdate() {
        if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3f) {
            sendPacket(C03PacketPlayer(true))
            mc.thePlayer.fallDistance = 0f
        }
    }
}