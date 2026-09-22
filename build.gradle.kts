plugins {
    id("net.fabricmc.fabric-loom")
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

version = "${property("mod.version")}+mc${property("mod.mc_targets")}"
group = project.findProperty("maven_group") as String
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.notenoughupdates.org/releases/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/")
    maven("https://jitpack.io/")
    mavenCentral()
}

val shadowImpl: Configuration = configurations.create("shadowImpl") {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:${property("mcVersion")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    implementation(libs.fabric.loader)
    implementation(libs.fabric.kotlin)
    runtimeOnly(libs.devauth)

    implementation("com.terraformersmc:modmenu:${property("deps.mod_menu")}")

    shadowImpl("org.notenoughupdates.moulconfig:modern-${property("deps.moulconfig")}")

    implementation(libs.commodore)
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}


kotlin {
    jvmToolchain(25)

    compilerOptions {
        freeCompilerArgs.add("-Xlambdas=class")
    }
}

loom {
    // From stonecutter
    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

tasks.shadowJar {
    archiveClassifier.set("")

    from("LICENSE") {
        rename { "${it}_${project.extra["archives_base_name"]}" }
    }

    configurations = listOf(shadowImpl)

    exclude("META-INF/versions/**")
    mergeServiceFiles()

    relocate("io.github.notenoughupdates.moulconfig", "io.github.frostzie.skyfall.deps.moulconfig")

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