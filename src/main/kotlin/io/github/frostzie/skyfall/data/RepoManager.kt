package io.github.frostzie.skyfall.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.frostzie.skyfall.utils.LoggerProvider
import kotlinx.coroutines.*
import net.fabricmc.loader.api.FabricLoader
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.*

// Taken and modified from Skyhanni
object RepoManager {
    private val logger = LoggerProvider.getLogger("RepoManager")
    private val gson = Gson()

    private const val REPO_USER = "Frostzie"
    private const val REPO_NAME = "SkyFall-REPO"
    private const val REPO_BRANCH = "main"

    private val configDir: Path = FabricLoader.getInstance().configDir.resolve("skyfall")
    private val repoDir: Path = configDir.resolve("repo")
    private val commitInfoFile: Path = configDir.resolve("commit-info.json")

    private var repoDownloadFailed = false
    private var currentCommitHash: String? = null
    private var currentCommitTime: Long? = null
    private var isCurrentlyFetching = false

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        try {
            configDir.createDirectories()
            repoDir.createDirectories()
        } catch (e: Exception) {
            logger.error("Failed to create config directories", e)
        }
    }

    fun initializeRepo() {
        repoScope.launch {
            try {
                loadCurrentCommitInfo()
                if (shouldUpdateRepo()) {
                    fetchRepository()
                }
            } catch (e: Exception) {
                logger.error("Failed to initialize repository", e)
                repoDownloadFailed = true
            }
        }
    }

    fun forceUpdateRepo() {
        repoScope.launch {
            logger.info("Force updating repository...")
            fetchRepository()
        }
    }

    private suspend fun shouldUpdateRepo(): Boolean {
        if (!repoDir.exists() || repoDir.listDirectoryEntries().isEmpty()) {
            logger.info("Repository directory is empty, need to download")
            return true
        }

        return try {
            val latestCommitInfo = getLatestCommitInfo()
            latestCommitInfo?.let { (latestHash, _) ->
                val needsUpdate = latestHash != currentCommitHash
                if (needsUpdate) {
                    logger.info("Repository needs update. Current: {}, Latest: {}",
                        currentCommitHash, latestHash)
                }
                needsUpdate
            } ?: false
        } catch (e: Exception) {
            logger.error("Failed to check if repo needs update", e)
            false
        }
    }

    private suspend fun getLatestCommitInfo(): Pair<String, Long>? = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.github.com/repos/$REPO_USER/$REPO_NAME/commits/$REPO_BRANCH"

        try {
            val connection = URI(apiUrl).toURL().openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 15000
                setRequestProperty("User-Agent", "SkyFall")
            }

            if (connection.responseCode != 200) {
                logger.warn("GitHub API returned status: {}", connection.responseCode)
                return@withContext null
            }

            connection.inputStream.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val response = JsonParser.parseReader(reader).asJsonObject
                    val hash = response.get("sha").asString
                    val dateString = response.getAsJsonObject("commit")
                        .getAsJsonObject("committer")
                        .get("date").asString
                    val time = Instant.parse(dateString).toEpochMilli()

                    hash to time
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to get latest commit info", e)
            null
        }
    }

    private suspend fun fetchRepository() {
        if (isCurrentlyFetching) return
        isCurrentlyFetching = true

        try {
            doTheFetching()
        } finally {
            isCurrentlyFetching = false
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun doTheFetching() = withContext(Dispatchers.IO) {
        try {
            val latestCommitInfo = getLatestCommitInfo()
            if (latestCommitInfo == null) {
                logger.error("Failed to get latest commit info")
                repoDownloadFailed = true
                return@withContext
            }

            val (latestCommitHash, latestCommitTime) = latestCommitInfo

            if (repoDir.exists() &&
                currentCommitHash == latestCommitHash &&
                repoDir.listDirectoryEntries().isNotEmpty()) {
                logger.info("Repository is already up to date")
                return@withContext
            }

            logger.info("Downloading repository commit: {}", latestCommitHash)

            val downloadUrl = "https://github.com/$REPO_USER/$REPO_NAME/archive/$latestCommitHash.zip"
            val tempZip = configDir.resolve("temp-repo.zip")

            downloadFile(downloadUrl, tempZip)

            if (repoDir.exists()) {
                repoDir.deleteRecursively()
            }
            repoDir.createDirectories()

            extractZipIgnoreFirstFolder(tempZip, repoDir)

            tempZip.deleteIfExists()

            saveCommitInfo(latestCommitHash, latestCommitTime)
            currentCommitHash = latestCommitHash
            currentCommitTime = latestCommitTime

            logger.info("Successfully downloaded and extracted repository")
            repoDownloadFailed = false

        } catch (e: Exception) {
            logger.error("Failed to download repository", e)
            repoDownloadFailed = true
        }
    }

    private suspend fun downloadFile(url: String, destination: Path) = withContext(Dispatchers.IO) {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.apply {
            connectTimeout = 15000
            readTimeout = 30000
        }

        connection.inputStream.use { inputStream ->
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private suspend fun extractZipIgnoreFirstFolder(zipFile: Path, destination: Path) = withContext(Dispatchers.IO) {
        ZipInputStream(Files.newInputStream(zipFile)).use { zis ->
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val currentEntry = entry ?: continue
                if (currentEntry.isDirectory) continue

                val entryName = currentEntry.name
                val firstSlash = entryName.indexOf('/')
                if (firstSlash == -1) continue

                val relativePath = entryName.substring(firstSlash + 1)
                if (relativePath.isEmpty()) continue

                val outputPath = destination.resolve(relativePath)
                outputPath.parent?.createDirectories()

                Files.newOutputStream(outputPath).use { out ->
                    val buffer = ByteArray(8192)
                    var length: Int
                    while (zis.read(buffer).also { length = it } != -1) {
                        out.write(buffer, 0, length)
                    }
                }
            }
        }
    }

    private fun loadCurrentCommitInfo() {
        if (!commitInfoFile.exists()) return

        try {
            commitInfoFile.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                val json = JsonParser.parseReader(reader).asJsonObject
                currentCommitHash = if (json.has("hash")) json.get("hash").asString else null
                currentCommitTime = if (json.has("time")) json.get("time").asLong else null
            }
        } catch (e: Exception) {
            logger.warn("Failed to load commit info", e)
        }
    }

    private fun saveCommitInfo(hash: String, time: Long) {
        val json = JsonObject().apply {
            addProperty("hash", hash)
            addProperty("time", time)
        }

        try {
            commitInfoFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                gson.toJson(json, writer)
            }
        } catch (e: Exception) {
            logger.warn("Failed to save commit info", e)
        }
    }

    /**
     * Load a JSON file from the downloaded repo
     */
    fun loadJsonFile(relativePath: String): JsonObject? {
        val filePath = repoDir.resolve(relativePath)
        if (!filePath.exists()) {
            logger.warn("JSON file not found: {}", filePath)
            return null
        }

        return try {
            filePath.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                JsonParser.parseReader(reader).asJsonObject
            }
        } catch (e: Exception) {
            logger.error("Failed to load JSON file: {}", relativePath, e)
            null
        }
    }

    /**
     * Get all JSON files in a directory within the repo
     */
    fun getJsonFilesInDirectory(relativePath: String): List<JsonObject> {
        val dirPath = repoDir.resolve(relativePath)
        if (!dirPath.exists() || !dirPath.isDirectory()) {
            logger.warn("Directory not found: {}", dirPath)
            return emptyList()
        }

        return try {
            dirPath.listDirectoryEntries("*.json")
                .mapNotNull { file ->
                    try {
                        file.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                            JsonParser.parseReader(reader).asJsonObject
                        }
                    } catch (e: Exception) {
                        logger.warn("Failed to parse JSON file: {}", file, e)
                        null
                    }
                }
        } catch (e: Exception) {
            logger.error("Failed to list JSON files in directory: {}", relativePath, e)
            emptyList()
        }
    }

    /**
     * Check if the repository is healthy and ready to use
     */
    fun isRepoHealthy(): Boolean {
        return !repoDownloadFailed &&
                repoDir.exists() &&
                repoDir.listDirectoryEntries().isNotEmpty()
    }

    /**
     * Get the current commit hash
     */
    fun getCurrentCommitHash(): String? = currentCommitHash

    /**
     * Get the current commit time
     */
    fun getCurrentCommitTime(): Long? = currentCommitTime

    /**
     * Get repository status for debugging
     */
    fun getRepoStatus(): String {
        return buildString {
            appendLine("Repository Status:")
            appendLine("- Healthy: ${isRepoHealthy()}")
            appendLine("- Download Failed: $repoDownloadFailed")
            appendLine("- Currently Fetching: $isCurrentlyFetching")
            appendLine("- Current Commit: $currentCommitHash")
            appendLine("- Repo Directory: $repoDir")
            appendLine("- Files Count: ${if (repoDir.exists()) repoDir.listDirectoryEntries().size else 0}")
        }
    }
}