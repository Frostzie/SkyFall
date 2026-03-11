package io.github.frostzie.nodex.services.config

import com.fasterxml.jackson.databind.JsonNode
import io.github.frostzie.nodex.utils.ModVersionUtils

/**
 * Service responsible for transforming [JsonNode] data across version boundaries.
 */
class MigrationService {
    private val migrations = mutableListOf<Migration>()

    private data class Migration(
        val toVersion: String,
        val action: (JsonNode) -> JsonNode
    )

    /**
     * Registers a migration step targeting a specific version.
     * 
     * @param toVersion The version this migration achieves.
     * @param action The transformation logic.
     */
    fun register(toVersion: String, action: (JsonNode) -> JsonNode) {
        migrations.add(Migration(toVersion, action))
    }

    /**
     * Migrates the given [node] from [storedVersion] to the latest registered version.
     * 
     * Applies all migrations where the target version is greater than [storedVersion],
     * sorted by the target version in ascending order.
     */
    fun migrate(node: JsonNode, storedVersion: String): JsonNode {
        var currentNode = node
        
        // Only care about migrations newer than what's stored in the config (idc about backwards compatibility rn)
        val applicable = migrations
            .filter { ModVersionUtils.isNewerThan(it.toVersion, storedVersion) }
            .sortedWith { m1, m2 -> ModVersionUtils.compare(m1.toVersion, m2.toVersion) }
        
        applicable.forEach { migration ->
            currentNode = migration.action(currentNode)
        }
        
        return currentNode
    }
}
