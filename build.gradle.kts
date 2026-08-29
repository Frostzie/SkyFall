plugins {
    id("net.fabricmc.fabric-loom")
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
}

version = "${property("mod.version")}+mc${property("mod.mc_targets")}"
group = project.findProperty("maven_group") as String
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.notenoughupdates.org/releases/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/")
    mavenCentral()
}

// Slightly modified from https://notenoughupdates.org/MoulConfig/
val shadowModImpl by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:26.2")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    implementation(libs.fabric.loader)
    implementation(libs.fabric.kotlin)
    runtimeOnly(libs.devauth)

    implementation("com.terraformersmc:modmenu:${property("deps.mod_menu")}")

    shadowModImpl("org.notenoughupdates.moulconfig:${property("deps.moulconfig")}")
}

// Taken from https://notenoughupdates.org/MoulConfig/
tasks.shadowJar {
    configurations = listOf(shadowModImpl)
    relocate("io.github.notenoughupdates.moulconfig", "io.github.frostzie.skyfall.deps.moulconfig")
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


kotlin {
    jvmToolchain(25)
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run" // Shares the run directory between versions
    }
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${project.extra["archives_base_name"]}" }
    }

    exclude("module-info.class")
    exclude("**/module-info.class")
    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")

    doLast {
        copy {
            from(outputs.files)
            into(rootProject.file("build/libs"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.findProperty("archives_base_name") as String
            from(components["java"])
        }
    }
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}