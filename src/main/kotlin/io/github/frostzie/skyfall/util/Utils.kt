package io.github.frostzie.skyfall.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

fun String.isColored(vararg formats: ChatFormatting): MutableComponent {
    return Component.literal(this).withStyle(*formats)
}