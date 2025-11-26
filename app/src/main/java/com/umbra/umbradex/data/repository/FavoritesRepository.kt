package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


@Serializable
data class UserFavorite(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("pokemon_id")
    val pokemonId: Int
)

class FavoritesRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get all user favorites
    suspend fun getUserFavorites(userId: String): NetworkResult<List<Int>> {
        return try {
            val result = supabase.from("user_favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserFavorite>()

            NetworkResult.Success(result.map { it.pokemonId })
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch favorites")
        }
    }

    // Add to favorites
    suspend fun addToFavorites(userId: String, pokemonId: Int): NetworkResult<Unit> {
        return try {
            supabase.from("user_favorites")
                .insert(buildJsonObject {
                    put("user_id", userId)
                    put("pokemon_id", pokemonId)
                })

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to add to favorites")
        }
    }

    // Remove from favorites
    suspend fun removeFromFavorites(userId: String, pokemonId: Int): NetworkResult<Unit> {
        return try {
            supabase.from("user_favorites")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("pokemon_id", pokemonId)
                    }
                }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to remove from favorites")
        }
    }

    // Check if Pokemon is favorite
    suspend fun isFavorite(userId: String, pokemonId: Int): NetworkResult<Boolean> {
        return try {
            val result = supabase.from("user_favorites")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("pokemon_id", pokemonId)
                    }
                }
                .decodeList<UserFavorite>()

            NetworkResult.Success(result.isNotEmpty())
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to check favorite status")
        }
    }
}