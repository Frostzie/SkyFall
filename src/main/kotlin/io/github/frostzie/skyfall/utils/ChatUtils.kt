package io.github.frostzie.skyfall.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent // Ensure this is the correct import
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor

object ChatUtils {

    private fun skyFallPrefix(): MutableText {
        val text = "SkyFall"
        val colors = listOf(
            0x00D0DD, // S
            0x00BFD8, // k
            0x00AED2, // y
            0x009DCD, // F
            0x008CC7, // a
            0x007BC2, // l
            0x006ABC  // l
        )
        val result = Text.empty()
        for (i in text.indices) {
            result.append(
                Text.literal(text[i].toString())
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colors[i])))
            )
        }
        return result
    }

    private val CHAT_PREFIX_STRING: MutableText = skyFallPrefix()
        .append(Text.literal(" §l§8» ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))))

    fun messageToChat(message: String, style: Style = Style.EMPTY) {
        val styledMessage = Text.literal(message).setStyle(style)
        val fullMessage = Text.empty()
            .append(CHAT_PREFIX_STRING)
            .append(styledMessage)
        MinecraftClient.getInstance().player?.sendMessage(fullMessage, false)
    }

    fun messageToChat(message: String): MessageBuilder {
        return MessageBuilder(message)
    }

    fun warning(warningMessage: String) {
        val styledMessage = Text.literal(warningMessage)
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00))) // Yellow
        val fullMessage = Text.empty()
            .append(CHAT_PREFIX_STRING)
            .append(styledMessage)
        MinecraftClient.getInstance().player?.sendMessage(fullMessage, false)
    }

    fun error(errorMessage: String, copyableText: String = errorMessage) {
        val styledMessage = Text.literal(errorMessage)
            .setStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555)) // Light Red
                    .withClickEvent(ClickEvent.CopyToClipboard(copyableText))
                    // Corrected HoverEvent instantiation
                    .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to copy error message")))
            )
        val fullMessage = Text.empty()
            .append(CHAT_PREFIX_STRING)
            .append(Text.literal("§l[Error]§r ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555))))
            .append(styledMessage)
        MinecraftClient.getInstance().player?.sendMessage(fullMessage, false)
    }

    class MessageBuilder(private val message: String) {
        private var style: Style = Style.EMPTY

        fun copyContent(copyText: String): MessageBuilder {
            style = style.withClickEvent(ClickEvent.CopyToClipboard(copyText))
                // Corrected HoverEvent instantiation
                .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to copy")))
            return this
        }

        fun clickToRun(command: String): MessageBuilder {
            style = style.withClickEvent(ClickEvent.RunCommand(command))
                // Corrected HoverEvent instantiation
                .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to run:§a\n$command")))
            return this
        }

        fun send() {
            messageToChat(message, style)
        }
    }
}