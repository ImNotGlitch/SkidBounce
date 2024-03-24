/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory.COMBAT
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*

object SuperKnockback : Module("SuperKnockback", COMBAT) {

    private val delay by IntegerValue("Delay", 0, 0, 500)
    private val hurtTime by IntegerValue("HurtTime", 10, 0, 10)

    private val mode by ListValue(
        "Mode",
        arrayOf("SprintTap", "WTap", "STap", "Old", "Silent", "Packet", "SneakPacket").sortedArray(),
        "Old"
    )
    private val maxTicksUntilBlock: IntegerValue = object : IntegerValue("MaxTicksUntilBlock", 2, 0..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minTicksUntilBlock.get())
    }
    private val minTicksUntilBlock: IntegerValue = object : IntegerValue("MinTicksUntilBlock", 0, 0..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxTicksUntilBlock.get())
    }

    private val reSprintMaxTicks: IntegerValue = object : IntegerValue("ReSprintMaxTicks", 2, 1..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(reSprintMinTicks.get())
    }
    private val reSprintMinTicks: IntegerValue = object : IntegerValue("ReSprintMinTicks", 1, 1..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(reSprintMaxTicks.get())
    }

    private val pressBackTicks: IntegerValue = object : IntegerValue("PressBackTicks", 1, 1..5) {
        override fun isSupported() = mode == "STap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(releaseBackTicks.get())
    }
    private val releaseBackTicks: IntegerValue = object : IntegerValue("ReleaseBackTicks", 2, 1..5) {
        override fun isSupported() = mode == "STap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(pressBackTicks.get())
    }

    private val onlyGround by BoolValue("OnlyGround", false)
    val onlyMove by BoolValue("OnlyMove", true)
    val onlyMoveForward by BoolValue("OnlyMoveForward", true) { onlyMove }

    private var ticks = 0
    private var forceSprintState = 0
    private val timer = MSTimer()

    // WTap
    private var blockInputTicks = randomDelay(minTicksUntilBlock.get(), maxTicksUntilBlock.get())
    private var blockTicksElapsed = 0
    private var startWaiting = false
    private var blockInput = false
    private var allowInputTicks = randomDelay(reSprintMinTicks.get(), reSprintMaxTicks.get())
    private var ticksElapsed = 0

    // STap
    private var pressTicks = 0

    override fun onToggle(state: Boolean) {
        // Make sure the user won't have their input forever blocked
        blockInput = false
        startWaiting = false
        blockTicksElapsed = 0
        ticksElapsed = 0
        pressTicks = 0
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val player = mc.thePlayer ?: return

        if (event.targetEntity !is EntityLivingBase) return

        if (event.targetEntity.hurtTime > hurtTime || !timer.hasTimePassed(delay) || (onlyGround && !player.onGround)) return

        if (onlyMove && (!isMoving || (onlyMoveForward && player.movementInput.moveStrafe != 0f))) return

        when (mode) {
            "Old" -> {
                // Users reported that this mode is better than the other ones

                if (mc.thePlayer.isSprinting) {
                    sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING))
                }

                sendPackets(
                    C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING),
                    C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING),
                    C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING)
                )
                mc.thePlayer.isSprinting = true
                mc.thePlayer.serverSprintState = true
            }

            "SprintTap", "Silent" -> if (player.isSprinting && player.serverSprintState) ticks = 2

            "Packet" -> {
                sendPackets(
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SPRINTING)
                )
            }

            "SneakPacket" -> {
                sendPackets(
                    C0BPacketEntityAction(player, STOP_SPRINTING),
                    C0BPacketEntityAction(player, START_SNEAKING),
                    C0BPacketEntityAction(player, START_SPRINTING),
                    C0BPacketEntityAction(player, STOP_SNEAKING)
                )
            }

            "WTap" -> {
                // We want the player to be sprinting before we block inputs
                if (player.isSprinting && player.serverSprintState && !blockInput && !startWaiting) {
                    blockInputTicks = randomDelay(minTicksUntilBlock.get(), maxTicksUntilBlock.get())

                    blockInput = blockInputTicks == 0

                    if (!blockInput) {
                        startWaiting = true
                    }

                    allowInputTicks = randomDelay(reSprintMinTicks.get(), reSprintMaxTicks.get())
                }
            }

            "STap" -> {
                if (++pressTicks == pressBackTicks.get()) {
                    if (player.isSprinting && player.serverSprintState) {
                        C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING)
                    }

                    if (!mc.gameSettings.keyBindBack.isKeyDown) {
                        mc.gameSettings.keyBindForward.pressed = false
                        mc.gameSettings.keyBindBack.pressed = true
                    }

                } else if (pressTicks >= releaseBackTicks.get()) {

                    mc.gameSettings.keyBindBack.pressed = false
                    player.isSprinting = true
                    player.serverSprintState = true

                    pressTicks = 0
                }
            }
        }

        timer.reset()
    }

    @EventTarget
    fun onPostSprintUpdate(event: PostSprintUpdateEvent) {
        val player = mc.thePlayer ?: return
        if (mode == "SprintTap") {
            when (ticks) {
                2 -> {
                    player.isSprinting = false
                    forceSprintState = 2
                    ticks--
                }

                1 -> {
                    if (player.movementInput.moveForward > 0.8) {
                        player.isSprinting = true
                    }
                    forceSprintState = 1
                    ticks--
                }

                else -> {
                    forceSprintState = 0
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mode == "WTap") {
            if (blockInput) {
                if (ticksElapsed++ >= allowInputTicks) {
                    blockInput = false
                    ticksElapsed = 0
                }
            } else {
                if (startWaiting) {
                    blockInput = blockTicksElapsed++ >= blockInputTicks

                    if (blockInput) {
                        startWaiting = false
                        blockTicksElapsed = 0
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet
        if (packet is C03PacketPlayer && mode == "Silent") {
            if (ticks == 2) {
                sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                ticks--
            } else if (ticks == 1 && player.isSprinting) {
                sendPacket(C0BPacketEntityAction(player, START_SPRINTING))
                ticks--
            }
        }
    }

    fun shouldBlockInput() = handleEvents() && mode == "WTap" && blockInput

    override val tag
        get() = mode


    fun breakSprint() = handleEvents() && forceSprintState == 2 && mode == "SprintTap"
    fun startSprint() = handleEvents() && forceSprintState == 1 && mode == "SprintTap"
}
