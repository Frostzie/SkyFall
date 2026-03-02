package io.github.frostzie.nodex.domain.config

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.nio.file.Path

/**
 * Domain state for the application's configuration path.
 */
class ConfigState(initialPath: Path) {
    /**
     * The root directory for all application configuration files.
     */
    val configDirProperty: ObjectProperty<Path> = SimpleObjectProperty(initialPath)
    
    var configDir: Path
        get() = configDirProperty.get()
        set(value) = configDirProperty.set(value)
}
