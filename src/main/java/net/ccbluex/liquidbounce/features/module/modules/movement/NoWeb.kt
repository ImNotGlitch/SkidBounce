/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other.*
import net.ccbluex.liquidbounce.value.ListValue

object NoWeb : Module("NoWeb", ModuleCategory.MOVEMENT) {

    private val noWebModes = arrayOf(
        // Vanilla
        Vanilla,

        // AAC
        AAC, LAAC,
        
        // Other
        Rewinside,
        MineBlaze,
        FastFall
    )

    private val modes = noWebModes.map { it.modeName }.toTypedArray()

    val mode by ListValue(
        "Mode", modes, "Vanilla"
    )

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        modeModule.onUpdate()
    }

    override val tag
        get() = mode

    private val modeModule
        get() = noWebModes.find { it.modeName == mode }!!
}
