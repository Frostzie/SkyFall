// Taken and modified from Firmament
plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.fabric.loom)
}

version = (libs.versions.version.get()) + "-" + (libs.versions.minecraft.get())
group = project.findProperty("maven_group") as String

base {
    archivesName.set(project.findProperty("archives_base_name") as String)
}

repositories {
    mavenLocal()
    maven("https://maven.notenoughupdates.org/releases/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn)
    modCompileOnly(libs.fabric.api)
    implementation(libs.gson)
    //modImplementation(libs.modmenu) //TODO: add when modmenu is available
    modImplementation(libs.moulconfig)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.kotlin)
    modImplementation(libs.fabric.api)
    modRuntimeOnly(libs.devauth)
    implementation("org.reflections:reflections:0.10.2") // Might switch to something like ClassGraph
    include("org.reflections:reflections:0.10.2")
    modImplementation("org.javassist:javassist:3.28.0-GA")
    include("org.javassist:javassist:3.28.0-GA")
    include(libs.moulconfig)
}

loom {
    accessWidenerPath.set(file("src/main/resources/skyfall.accesswidener"))
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

java {
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    from("LICENSE") {
        rename { "${it}_${project.extra["archives_base_name"]}" }
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