package io.github.frostzie.skyfall.config.features;

import com.google.gson.annotations.Expose;
import io.github.frostzie.skyfall.utils.WebUtils;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class About {

//    @ConfigOption(name = "Current Version", desc = "This is your current version of SkyFall")
//    public transient Void currentVersion = null;

//    @ConfigOption(name = "Auto Update", desc = "Automatically check for updates on startup")
//    @Expose
//    @ConfigEditorBoolean
//    public boolean autoUpdate = true;


    public static class Licenses {
        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable moulConfig = () -> WebUtils.openBrowser("https://github.com/NotEnoughUpdates/MoulConfig");

        @ConfigOption(name = "NotEnoughUpdates", desc = "NotEnoughUpdates is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable notEnoughUpdates = () -> WebUtils.openBrowser("https://github.com/NotEnoughUpdates/NotEnoughUpdates");

        @ConfigOption(name = "SkyHanni", desc = "SkyHanni is available under the GNU Lesser General Public License v2.1")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable skyhanni = () -> WebUtils.openBrowser("https://github.com/hannibal002/SkyHanni");

        @ConfigOption(name = "Forge", desc = "Forge is available under the LGPL 3.0 license")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable forge = () -> WebUtils.openBrowser("https://github.com/MinecraftForge/MinecraftForge");

        @ConfigOption(name = "LibAutoUpdate", desc = "LibAutoUpdate is available under the BSD 2 Clause License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable libAutoUpdate = () -> WebUtils.openBrowser("https://git.nea.moe/nea/libautoupdate/");

        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        public Runnable mixin = () -> WebUtils.openBrowser("https://github.com/SpongePowered/Mixin/");
    }
}
