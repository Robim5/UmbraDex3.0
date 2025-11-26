package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.Team
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class TeamRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get all user teams
    suspend fun getUserTeams(userId: String): NetworkResult<List<Team>> {
        return try {
            val result = supabase.from("user_teams")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Team>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch teams")
        }
    }

    // Create new team
    suspend fun createTeam(
        userId: String,
        name: String,
        region: String?,
        pokemonIds: List<Int>
    ): NetworkResult<Team> {
        return try {
            if (pokemonIds.size > 6) {
                return NetworkResult.Error("Team cannot have more than 6 Pokemon")
            }

            val team = buildJsonObject {
                put("user_id", userId)
                put("name", name)
                region?.let { put("region", it) }
                putJsonArray("pokemon_ids") {
                    pokemonIds.forEach { add(JsonPrimitive(it)) }
                }
            }

            val result = supabase.from("user_teams")
                .insert(team)
                .decodeSingle<Team>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to create team")
        }
    }

    // Update team
    suspend fun updateTeam(
        teamId: String,
        name: String?,
        region: String?,
        pokemonIds: List<Int>?
    ): NetworkResult<Team> {
        return try {
            pokemonIds?.let {
                if (it.size > 6) {
                    return NetworkResult.Error("Team cannot have more than 6 Pokemon")
                }
            }

            val updates = buildJsonObject {
                name?.let { put("name", it) }
                region?.let { put("region", it) }
                pokemonIds?.let {
                    putJsonArray("pokemon_ids") {
                        it.forEach { id -> add(JsonPrimitive(id)) }
                    }
                }
            }

            val result = supabase.from("user_teams")
                .update(updates) {
                    filter {
                        eq("id", teamId)
                    }
                }
                .decodeSingle<Team>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update team")
        }
    }

    // Delete team
    suspend fun deleteTeam(teamId: String): NetworkResult<Unit> {
        return try {
            supabase.from("user_teams")
                .delete {
                    filter {
                        eq("id", teamId)
                    }
                }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to delete team")
        }
    }

    // Get single team
    suspend fun getTeam(teamId: String): NetworkResult<Team> {
        return try {
            val result = supabase.from("user_teams")
                .select {
                    filter {
                        eq("id", teamId)
                    }
                }
                .decodeSingle<Team>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch team")
        }
    }
}