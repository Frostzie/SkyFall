package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.utils.ColorUtils.skyFallPrefixChat
import net.minecraft.client.MinecraftClient
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor

object ChatUtils {
    private val CHAT_PREFIX_STRING: MutableText = skyFallPrefixChat()
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
                .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to copy")))
            return this
        }

        fun openLink(url: String): MessageBuilder {
            style = style.withClickEvent(ClickEvent.OpenUrl(java.net.URI.create(url)))
                .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to open:§a\n$url")))
            return this
        }

        fun clickToRun(command: String): MessageBuilder {
            style = style.withClickEvent(ClickEvent.RunCommand(command))
                .withHoverEvent(HoverEvent.ShowText(Text.literal("§eClick to run:§a\n$command")))
            return this
        }

        fun send() {
            messageToChat(message, style)
        }
    }
}