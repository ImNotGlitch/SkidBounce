/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.minecraft.network.play.server.S32PacketConfirmTransaction

/**
 * @author EclipsesDev
 * @author CCBlueX/LiquidBounce
 */
object Vulcan : VelocityMode("Vulcan") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is S32PacketConfirmTransaction)
            event.cancelEvent()
    }

    override fun onVelocityPacket(event: PacketEvent) = event.cancelEvent()
}
