package io.github.frostzie.skyfall.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import java.net.HttpURLConnection
import java.net.URL

//TODO: add auto update.
/**
 * Checks for new mod versions from Modrinth.
 * It performs the check asynchronously and notifies the user upon joining a world
 * if a new version is found.
 */
object UpdateChecker {
    private val logger = LoggerProvider.getLogger("UpdateChecker")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var newVersionAvailable: String? = null
    private var hasShownUpdateNotification = false

    /**
     * Initializes the update checker. It starts the check in the background
     * and registers an event listener to show the notification upon joining a world.
     */
    fun initialize() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            showUpdateNotificationIfNeeded()
        }
        checkForUpdates()
    }

    /**
     * Displays the update notification if a new version is available and it hasn't been shown yet.
     * This is triggered by the ClientPlayConnectionEvents.JOIN event.
     */
    private fun showUpdateNotificationIfNeeded() {
        if (newVersionAvailable != null && !hasShownUpdateNotification) {
            val currentVersion = getCurrentVersion()
            val latestVersion = newVersionAvailable!!

            val downloadUrl = "https://modrinth.com/mod/skyfall"
            ChatUtils.messageToChat("§aA new version of SkyFall is available! Click here to download.")
                .openLink(downloadUrl)
                .send()
            ChatUtils.messageToChat("§7Current: §c$currentVersion §7→ §7Latest: §a$latestVersion")

            hasShownUpdateNotification = true
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            FabricLoader.getInstance()
                .getModContainer("skyfall")
                .map { it.metadata.version.friendlyString }
                .orElse("unknown")
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun checkForUpdates() {
        scope.launch {
            try {
                val currentVersion = getCurrentVersion()
                if (currentVersion == "unknown") {
                    logger.error("Could not determine current version, aborting update check.")
                    return@launch
                }

                val url = URL("https://api.modrinth.com/v2/project/skyfall/version")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "SkyFall-Mod/$currentVersion")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode

                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val gson = Gson()
                    val jsonArray = gson.fromJson(response, JsonArray::class.java)

                    if (jsonArray.size() > 0) {
                        val latestVersionObj = jsonArray.get(0).asJsonObject
                        val latestVersionNumber = latestVersionObj.get("version_number").asString

                        if (latestVersionNumber != currentVersion && !latestVersionNumber.contains(currentVersion)) {
                            newVersionAvailable = latestVersionNumber
                        }
                    } else {
                        logger.warn("Modrinth API returned an empty array for project versions.")
                    }
                } else {
                    logger.warn("Failed to fetch updates. Response code: {}. Reading error stream...", responseCode)
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    logger.warn("Error response from Modrinth API: {}", errorResponse)
                }
            } catch (e: Exception) {
                logger.error("Failed to check for updates due to an exception.", e)
            }
        }
    }
}