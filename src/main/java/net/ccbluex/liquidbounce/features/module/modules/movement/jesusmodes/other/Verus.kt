/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.jesusmodes.other

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesusmodes.JesusMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.extensions.*
import net.minecraft.block.BlockLiquid
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB.fromBounds

object Verus : JesusMode("Verus", true) {
    private var nextTick = true

    override fun onPacket(event: PacketEvent) {
        mc.thePlayer ?: return

        if (event.packet !is C03PacketPlayer) return

        val (maxX, maxY, maxZ, minX, minY, minZ) = mc.thePlayer.entityBoundingBox

        if (collideBlock(
                fromBounds(
                    maxX,
                    maxY,
                    maxZ,
                    minX,
                    minY - 0.01,
                    minZ
                )
            ) { it is BlockLiquid }
        ) {
            event.packet.onGround = nextTick
            nextTick = !nextTick
        } else nextTick = true
    }
}
