/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.event.events

import net.ccbluex.liquidbounce.event.Event

/**
 * Called when world is going to be rendered
 */
class Render3DEvent(val partialTicks: Float) : Event()
