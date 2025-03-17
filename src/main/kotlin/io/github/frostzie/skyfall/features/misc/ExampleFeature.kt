package io.github.frostzie.skyfall.features.misc

import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

class ExampleFeature {
    companion object {
        fun register() {
            ClientCommandRegistrationCallback.EVENT.register { a, _ ->
                a.register(literal("sftest").executes {
                    if (SkyFall.feature.miscFeatures.enabled) {
                        println("working")
                    } else {
                        println("Feature is disabled")
                    }
                    0
                })
            }
        }
    }
}