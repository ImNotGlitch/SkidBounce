/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.verus

import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.jmp
import net.minecraft.potion.Potion

/**
 * @author CCBlueX/LiquidBounce
 */
object VerusHop : SpeedMode("VerusHop") {
    private var speed = 0.0f

    override fun onUpdate() {
        if (mc.thePlayer == null) {
            return
        }
        if (isMoving) {
            if (mc.thePlayer.onGround) {
                speed = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)
                    && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier >= 1
                )
                    0.46f else 0.34f

                mc.thePlayer.jmp()
            } else {
                speed *= 0.98f
            }

            strafe(speed, false)
        }
    }
}
