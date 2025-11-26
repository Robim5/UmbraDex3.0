package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.OnboardingData
import com.umbra.umbradex.data.model.User
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val supabase = UmbraDexApplication.supabase

    // Sign up with email and password
    suspend fun signUp(email: String, password: String): NetworkResult<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Sign up failed")
        }
    }

    // Login with email and password
    suspend fun login(email: String, password: String): NetworkResult<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Login failed")
        }
    }

    // Create user profile after signup
    suspend fun createProfile(onboardingData: OnboardingData): NetworkResult<User> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return NetworkResult.Error("User not authenticated")

            val profile = buildJsonObject {
                put("id", userId)
                put("username", onboardingData.username)
                put("birth_date", onboardingData.birthDate)
                put("pokemon_knowledge", onboardingData.pokemonKnowledge)
                put("favorite_type", onboardingData.favoriteType)
                put("avatar_url", onboardingData.avatarUrl)
                put("equipped_starter", onboardingData.starterId)
                put("gold", 100)
                put("level", 1)
                put("xp", 0)
            }

            val result = supabase.from("profiles")
                .insert(profile)
                .decodeSingle<User>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to create profile")
        }
    }

    // Logout
    suspend fun logout(): NetworkResult<Unit> {
        return try {
            supabase.auth.signOut()
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Logout failed")
        }
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
}