package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
class AuthRepository {
    private val auth = UmbraSupabase.auth

    suspend fun login(email: String, password: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            auth.signInWith(Email) {
                this.email = email      // Define o email
                this.password = password // Define a password
            }
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Login failed: ${e.message}"))
        }
    }
    suspend fun signup(
        email: String,
        password: String,
        username: String,
        birthDate: String,
        pokemonKnowledge: String,
        favoriteType: String,
        avatar: String,
        starterId: Int
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password

                this.data = buildJsonObject {
                    put("username", username)
                    put("birth_date", birthDate)
                    put("pokemon_knowledge", pokemonKnowledge)
                    put("favorite_type", favoriteType)
                    put("avatar", avatar)
                    put("starter_id", starterId)
                }
            }
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Signup failed: ${e.message}"))
        }
    }
    suspend fun logout(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            auth.signOut()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Logout failed: ${e.message}"))
        }
    }
    fun isUserLoggedIn(): Boolean {
        return auth.currentUserOrNull() != null
    }
    fun getCurrentUserId(): String? {
        // Retorna o ID (String) ou null se não houver sessão
        return auth.currentUserOrNull()?.id
    }
}