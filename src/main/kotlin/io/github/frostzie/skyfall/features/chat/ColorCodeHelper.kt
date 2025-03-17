package io.github.frostzie.skyfall.features.chat



import io.github.frostzie.skyfall.config.Features
import io.github.notenoughupdates.moulconfig.Config
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

// TODO: Make it save to config and also fully set it up in the correct locations and move to main skyfall file - frostzie

object ColorCodeHelper : Features() {

 //   object Config : ManagedConfig("chat", Category.Chat){
   //     var ColorCodeHelper = false
  //  }






   // fun ColorCodeHelper (){
   //         if (!config.ColorCodeHelper) return


init {
        ClientCommandRegistrationCallback.EVENT.register { a, b ->
           a.register(literal("sfcolor").executes {
                val player = MinecraftClient.getInstance().player
                val message = Text.literal(
                    "§c===================================================\n" +
                    //        "§f&0 = §0Black              §f&1 = §1Dark Blue\n" +
                    //        "§f&2 = §2Dark Green      §f&3 = §3Dark Aqua\n" +
                    //        "§f&4 = §4Dark Red         §f&5 = §5Dark Purple\n" +
                    //        "§f&6 = §6Gold               §f&7 = §7Gray\n" +
                    //        "§f&8 = §8Dark Gray       §f&9 = §9Blue\n" +
                    //        "§f&a = §aGreen            §f&b = §bAqua\n" +
                    //        "§f&c = §cRed               §f&d = §dLight Purple\n" +
                    //        "§f&e = §eYellow            §f&f = §fWhite\n" +
                    //        "§f&Z = §zChroma §r(needs to enable chroma setting)\n" +
                    //        "§c================= Formatting Codes ==================\n" +
                    //        "§f&k = Obfuscated (like this: §khellspawn§r)\n" +
                    //        "§f&l = §lBold           §r&m = §mStrikethrough \n" +
                    //        "§f&o = §oItalic            §r&n = §nUnderline\n" +
                    //        "§f&r = Reset\n" +
                            " \n" +
                            "§fMoulConfig Instance Working\n" +
                            " \n" +
                            "§c==================================================="
                )
                player?.sendMessage(message, false)
                0
            })
        }
    }
}