pluginManagement {
	repositories {
		mavenLocal()
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "SkyFall"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("symbols")
include("javaplugin")
include("testagent")