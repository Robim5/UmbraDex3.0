package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Mission(
    val id: Int,
    val title: String,
    val description: String? = null,
    val rarity: String, // "common", "rare", "epic", "legendary"
    @SerialName("requirement_type")
    val requirementType: String, // "favorite_count", "purchase", "change_pet", etc.
    @SerialName("requirement_value")
    val requirementValue: Int,
    @SerialName("gold_reward")
    val goldReward: Int,
    @SerialName("xp_reward")
    val xpReward: Int
)

@Serializable
data class UserMission(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("mission_id")
    val missionId: Int,
    val progress: Int = 0,
    val completed: Boolean = false,
    @SerialName("completed_at")
    val completedAt: String? = null
)

// For displaying missions with progress
data class MissionWithProgress(
    val mission: Mission,
    val userMission: UserMission?
) {
    val isCompleted: Boolean
        get() = userMission?.completed == true

    val currentProgress: Int
        get() = userMission?.progress ?: 0

    val progressPercentage: Float
        get() = if (mission.requirementValue > 0) {
            (currentProgress.toFloat() / mission.requirementValue.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

// Mission requirement types
enum class MissionRequirement {
    FAVORITE_COUNT,        // Add X pokemon to favorites
    PURCHASE_ITEM,         // Buy X items
    CHANGE_PET,            // Change equipped pet X times
    COLLECT_POKEMON,       // Collect X pokemon in living dex
    COMPLETE_TEAM,         // Create X teams
    REACH_LEVEL,           // Reach level X
    EARN_GOLD,             // Earn X gold total
    COLLECT_TYPE,          // Collect X pokemon of a specific type
    COLLECT_GENERATION,    // Collect all pokemon from generation X
    EQUIP_LEGENDARY_ITEM;  // Equip a legendary item

    companion object {
        fun fromString(type: String): MissionRequirement {
            return when (type.lowercase()) {
                "favorite_count" -> FAVORITE_COUNT
                "purchase" -> PURCHASE_ITEM
                "change_pet" -> CHANGE_PET
                "collect_pokemon" -> COLLECT_POKEMON
                "complete_team" -> COMPLETE_TEAM
                "reach_level" -> REACH_LEVEL
                "earn_gold" -> EARN_GOLD
                "collect_type" -> COLLECT_TYPE
                "collect_generation" -> COLLECT_GENERATION
                "equip_legendary_item" -> EQUIP_LEGENDARY_ITEM
                else -> FAVORITE_COUNT
            }
        }
    }
}