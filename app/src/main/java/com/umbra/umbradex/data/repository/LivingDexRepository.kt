package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class UserLivingDex(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("pokemon_id")
    val pokemonId: Int,
    @SerialName("obtained_at")
    val obtainedAt: String? = null
)

class LivingDexRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get all Pokemon user owns
    suspend fun getUserLivingDex(userId: String): NetworkResult<List<Int>> {
        return try {
            val result = supabase.from("user_living_dex")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserLivingDex>()

            NetworkResult.Success(result.map { it.pokemonId })
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch living dex")
        }
    }

    // Add Pokemon to living dex
    suspend fun addToLivingDex(userId: String, pokemonId: Int): NetworkResult<Unit> {
        return try {
            supabase.from("user_living_dex")
                .insert(buildJsonObject {
                    put("user_id", userId)
                    put("pokemon_id", pokemonId)
                })

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add to living dex")
        }
    }

    // Remove from living dex
    suspend fun removeFromLivingDex(userId: String, pokemonId: Int): NetworkResult<Unit> {
        return try {
            supabase.from("user_living_dex")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("pokemon_id", pokemonId)
                    }
                }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to remove from living dex")
        }
    }

    // Check if user owns Pokemon
    suspend fun ownsPokem(userId: String, pokemonId: Int): NetworkResult<Boolean> {
        return try {
            val result = supabase.from("user_living_dex")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("pokemon_id", pokemonId)
                    }
                }
                .decodeList<UserLivingDex>()

            NetworkResult.Success(result.isNotEmpty())
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to check ownership")
        }
    }

    // Get stats: total owned, by type, by generation
    suspend fun getLivingDexStats(userId: String): NetworkResult<LivingDexStats> {
        return try {
            val ownedPokemon = getUserLivingDex(userId)
            if (ownedPokemon !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to get living dex")
            }

            val totalOwned = ownedPokemon.data!!.size
            val totalPossible = 1025
            val percentageComplete = (totalOwned.toFloat() / totalPossible * 100).toInt()

            NetworkResult.Success(
                LivingDexStats(
                    totalOwned = totalOwned,
                    totalPossible = totalPossible,
                    percentageComplete = percentageComplete,
                    remaining = totalPossible - totalOwned
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to calculate stats")
        }
    }
}

data class LivingDexStats(
    val totalOwned: Int,
    val totalPossible: Int,
    val percentageComplete: Int,
    val remaining: Int
)