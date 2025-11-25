package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    @SerialName("birth_date")
    val birthDate: String,
    @SerialName("pokemon_knowledge")
    val pokemonKnowledge: String, // "love", "some", "lot"
    @SerialName("favorite_type")
    val favoriteType: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("equipped_starter")
    val equippedStarter: Int? = null,
    val gold: Int = 100,
    val level: Int = 1,
    val xp: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class UserProfile(
    val user: User,
    val equippedPokemon: Pokemon? = null,
    val totalPokemonOwned: Int = 0,
    val totalFavorites: Int = 0,
    val completedMissions: Int = 0
)

// For onboarding/signup
data class OnboardingData(
    val birthDate: String,
    val pokemonKnowledge: String,
    val favoriteType: String,
    val avatarUrl: String,
    val username: String,
    val starterId: Int
)