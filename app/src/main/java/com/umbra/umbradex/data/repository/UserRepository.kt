package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.User
import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UserRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get user profile
    suspend fun getUserProfile(userId: String): NetworkResult<User> {
        return try {
            val result = supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch profile")
        }
    }

    // Update user profile
    suspend fun updateProfile(userId: String, updates: Map<String, Any>): NetworkResult<User> {
        return try {
            val updateJson = buildJsonObject {
                updates.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, value)
                        is Int -> put(key, value)
                        is Boolean -> put(key, value)
                    }
                }
            }

            val result = supabase.from("profiles")
                .update(updateJson) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update profile")
        }
    }

    // Update gold
    suspend fun updateGold(userId: String, newGold: Int): NetworkResult<Unit> {
        return try {
            supabase.from("profiles")
                .update(buildJsonObject {
                    put("gold", newGold)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update gold")
        }
    }

    // Update XP and level
    suspend fun updateXpAndLevel(userId: String, xp: Int, level: Int): NetworkResult<Unit> {
        return try {
            supabase.from("profiles")
                .update(buildJsonObject {
                    put("xp", xp)
                    put("level", level)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update XP")
        }
    }

    // Update equipped starter/pet
    suspend fun updateEquippedPokemon(userId: String, pokemonId: Int): NetworkResult<Unit> {
        return try {
            supabase.from("profiles")
                .update(buildJsonObject {
                    put("equipped_starter", pokemonId)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to update equipped pokemon")
        }
    }

    // Add gold (for rewards)
    suspend fun addGold(userId: String, amount: Int): NetworkResult<Unit> {
        return try {
            val profile = getUserProfile(userId)
            if (profile is NetworkResult.Success) {
                val newGold = profile.data!!.gold + amount
                updateGold(userId, newGold)
            } else {
                NetworkResult.Error("Failed to get current gold")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add gold")
        }
    }

    // Add XP (auto-level up)
    suspend fun addXp(userId: String, amount: Int): NetworkResult<Unit> {
        return try {
            val profile = getUserProfile(userId)
            if (profile is NetworkResult.Success) {
                val currentXp = profile.data!!.xp
                val currentLevel = profile.data!!.level
                val newXp = currentXp + amount

                // Calculate new level (100 XP per level)
                val newLevel = (newXp / 100).coerceAtLeast(1)

                updateXpAndLevel(userId, newXp, newLevel)
            } else {
                NetworkResult.Error("Failed to get current XP")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add XP")
        }
    }
}