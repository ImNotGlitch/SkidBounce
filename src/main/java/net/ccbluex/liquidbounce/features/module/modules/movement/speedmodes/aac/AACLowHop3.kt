/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.aac

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.direction
import net.ccbluex.liquidbounce.utils.MovementUtils.forward
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.speed
import net.ccbluex.liquidbounce.utils.extensions.jump
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author CCBlueX/LiquidBounce
 */
object AACLowHop3 : SpeedMode("AACLowHop3") {
    private var firstJump = false
    private var waitForGround = false
    override fun onEnable() {
        firstJump = true
    }

    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (isMoving) {
            if (thePlayer.hurtTime <= 0) {
                if (thePlayer.onGround) {
                    waitForGround = false
                    if (!firstJump) firstJump = true
                    thePlayer.jump(0.41)
                } else {
                    if (waitForGround) return
                    if (thePlayer.isCollidedHorizontally) return
                    firstJump = false
                    thePlayer.motionY -= 0.0149
                }
                if (!thePlayer.isCollidedHorizontally) forward(if (firstJump) 0.0016 else 0.001799)
            } else {
                firstJump = true
                waitForGround = true
            }
        } else {
            thePlayer.motionZ = 0.0
            thePlayer.motionX = 0.0
        }
        val speed = speed
        thePlayer.motionX = sin(direction) * -speed
        thePlayer.motionZ = cos(direction) * speed
    }
}
