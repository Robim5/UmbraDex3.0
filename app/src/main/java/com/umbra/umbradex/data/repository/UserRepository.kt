package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UserRepository {

    private val db = UmbraSupabase.db
    private val auth = UmbraSupabase.auth

    // Obter perfil do user atual
    fun getUserProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)
        try {
            val userId = UmbraSupabase.auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            val profile = db.from("profiles")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()

            emit(Resource.Success(profile))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load profile: ${e.message}"))
        }
    }

    // Atualizar nome de utilizador
    fun updateUsername(newUsername: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.from("profiles").update({
                set("username", newUsername)
            }) {
                filter {
                    eq("id", userId)
                }
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to update username: ${e.message}"))
        }
    }

    // Equipar Pokémon como pet
    fun equipPokemon(pokedexId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.from("profiles").update({
                set("equipped_pokemon_id", pokedexId)
            }) {
                filter {
                    eq("id", userId)
                }
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to equip pokemon: ${e.message}"))
        }
    }

    // Incrementar clicks no pet (para achievement)
    fun incrementPetClicks(): Flow<Resource<Boolean>> = flow {
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            // Usa RPC para incrementar atomicamente
            db.rpc("increment_pet_clicks", buildJsonObject {
                put("p_user_id", userId)
            })

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to increment clicks: ${e.message}"))
        }
    }

    // Adicionar Gold
    fun addGold(amount: Int): Flow<Resource<Boolean>> = flow {
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.rpc("add_gold", buildJsonObject {
                put("p_user_id", userId)
                put("p_amount", amount)
            })

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to add gold: ${e.message}"))
        }
    }

    // Subtrair Gold (para compras)
    fun spendGold(amount: Int): Flow<Resource<Boolean>> = flow {
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.rpc("spend_gold", buildJsonObject {
                put("p_user_id", userId)
                put("p_amount", amount)
            })

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to spend gold: ${e.message}"))
        }
    }

    // Adicionar XP (level up automático via trigger)
    fun addXP(amount: Int): Flow<Resource<Boolean>> = flow {
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.rpc("add_xp_and_level_up", buildJsonObject {
                put("p_user_id", userId)
                put("p_xp_amount", amount)
            })

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to add XP: ${e.message}"))
        }
    }
}