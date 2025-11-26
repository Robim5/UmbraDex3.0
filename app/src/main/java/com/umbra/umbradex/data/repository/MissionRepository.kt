package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.Mission
import com.umbra.umbradex.data.model.UserMission
import com.umbra.umbradex.data.model.MissionWithProgress
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MissionRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get all missions
    suspend fun getAllMissions(): NetworkResult<List<Mission>> {
        return try {
            val result = supabase.from("missions")
                .select()
                .decodeList<Mission>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch missions")
        }
    }

    // Get missions by rarity
    suspend fun getMissionsByRarity(rarity: String): NetworkResult<List<Mission>> {
        return try {
            val result = supabase.from("missions")
                .select {
                    filter {
                        eq("rarity", rarity)
                    }
                }
                .decodeList<Mission>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch missions by rarity")
        }
    }

    // Get user mission progress
    suspend fun getUserMissions(userId: String): NetworkResult<List<UserMission>> {
        return try {
            val result = supabase.from("user_missions")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserMission>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch user missions")
        }
    }

    // Get missions with progress
    suspend fun getMissionsWithProgress(userId: String): NetworkResult<List<MissionWithProgress>> {
        return try {
            val missions = getAllMissions()
            val userMissions = getUserMissions(userId)

            if (missions !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch missions")
            }
            if (userMissions !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch user missions")
            }

            val userMissionsMap = userMissions.data!!.associateBy { it.missionId }

            val missionsWithProgress = missions.data!!.map { mission ->
                MissionWithProgress(
                    mission = mission,
                    userMission = userMissionsMap[mission.id]
                )
            }

            NetworkResult.Success(missionsWithProgress)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch missions with progress")
        }
    }

    // Get incomplete missions (for display on missions page)
    suspend fun getIncompleteMissions(userId: String, limit: Int = 5): NetworkResult<List<MissionWithProgress>> {
        return try {
            val missionsWithProgress = getMissionsWithProgress(userId)

            if (missionsWithProgress !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch missions")
            }

            val incomplete = missionsWithProgress.data!!
                .filter { !it.isCompleted }
                .take(limit)

            NetworkResult.Success(incomplete)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch incomplete missions")
        }
    }

    // Initialize user mission progress (called after signup or when new missions are added)
    suspend fun initializeUserMission(userId: String, missionId: Int): NetworkResult<UserMission> {
        return try {
            val mission = buildJsonObject {
                put("user_id", userId)
                put("mission_id", missionId)
                put("progress", 0)
                put("completed", false)
            }

            val result = supabase.from("user_missions")
                .insert(mission)
                .decodeSingle<UserMission>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to initialize mission")
        }
    }

    // Update mission progress
    suspend fun updateMissionProgress(
        userId: String,
        missionId: Int,
        newProgress: Int
    ): NetworkResult<UserMission> {
        return try {
            // Get the mission to check requirement
            val missionResult = supabase.from("missions")
                .select {
                    filter {
                        eq("id", missionId)
                    }
                }
                .decodeSingle<Mission>()

            val isCompleted = newProgress >= missionResult.requirementValue

            val updates = buildJsonObject {
                put("progress", newProgress)
                put("completed", isCompleted)
                if (isCompleted) {
                    put("completed_at", java.time.Instant.now().toString())
                }
            }

            val result = supabase.from("user_missions")
                .update(updates) {
                    filter {
                        eq("user_id", userId)
                        eq("mission_id", missionId)
                    }
                }
                .decodeSingle<UserMission>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update mission progress")
        }
    }

    // Increment mission progress
    suspend fun incrementMissionProgress(
        userId: String,
        missionId: Int,
        incrementBy: Int = 1
    ): NetworkResult<UserMission> {
        return try {
            // Get current progress
            val currentMission = supabase.from("user_missions")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("mission_id", missionId)
                    }
                }
                .decodeSingle<UserMission>()

            val newProgress = currentMission.progress + incrementBy

            updateMissionProgress(userId, missionId, newProgress)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to increment mission progress")
        }
    }

    // Complete mission and claim rewards
    suspend fun claimMissionReward(userId: String, missionId: Int): NetworkResult<Mission> {
        return try {
            // Get mission details
            val mission = supabase.from("missions")
                .select {
                    filter {
                        eq("id", missionId)
                    }
                }
                .decodeSingle<Mission>()

            // Check if mission is completed
            val userMission = supabase.from("user_missions")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("mission_id", missionId)
                    }
                }
                .decodeSingle<UserMission>()

            if (!userMission.completed) {
                return NetworkResult.Error("Mission not completed yet")
            }

            // Give rewards (gold and XP)
            val userRepo = UserRepository()
            userRepo.addGold(userId, mission.goldReward)
            userRepo.addXp(userId, mission.xpReward)

            NetworkResult.Success(mission)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to claim mission reward")
        }
    }

    // Get mission statistics
    suspend fun getMissionStats(userId: String): NetworkResult<MissionStats> {
        return try {
            val userMissions = getUserMissions(userId)
            if (userMissions !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch user missions")
            }

            val total = userMissions.data!!.size
            val completed = userMissions.data.count { it.completed }
            val remaining = total - completed

            // Count by rarity
            val missions = getAllMissions()
            if (missions !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch missions")
            }

            val userMissionsMap = userMissions.data.associateBy { it.missionId }
            val completedMissions = missions.data!!.filter {
                userMissionsMap[it.id]?.completed == true
            }

            val legendaryCompleted = completedMissions.count { it.rarity == "legendary" }

            NetworkResult.Success(
                MissionStats(
                    total = total,
                    completed = completed,
                    remaining = remaining,
                    legendaryCompleted = legendaryCompleted,
                    completionPercentage = if (total > 0) (completed * 100 / total) else 0
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to calculate mission stats")
        }
    }
}

data class MissionStats(
    val total: Int,
    val completed: Int,
    val remaining: Int,
    val legendaryCompleted: Int,
    val completionPercentage: Int
)