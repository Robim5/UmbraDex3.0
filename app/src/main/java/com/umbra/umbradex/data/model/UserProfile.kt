package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val email: String,

    // Recursos
    val gold: Long = 0,
    val xp: Long = 0,
    val level: Int = 1,
    @SerialName("xp_for_next_level") val xpForNextLevel: Long = 60,

    // Equipamento
    @SerialName("equipped_pokemon_id") val equippedPokemonId: Int? = null,
    @SerialName("equipped_skin") val equippedSkin: String = "standard_male1",
    @SerialName("equipped_theme") val equippedTheme: String = "theme_default",
    @SerialName("equipped_badge") val equippedBadge: String = "start_badget",
    @SerialName("equipped_title") val equippedTitle: String = "Rookie",
    @SerialName("equipped_name_color") val equippedNameColor: String = "[\"#FFFFFF\"]", // JSON string

    // Onboarding
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("pokemon_knowledge") val pokemonKnowledge: String? = null,
    @SerialName("favorite_type") val favoriteType: String? = null,

    // Estatísticas
    @SerialName("total_time_seconds") val totalTimeSeconds: Long = 0,
    @SerialName("total_gold_earned") val totalGoldEarned: Long = 0,
    @SerialName("total_xp_earned") val totalXpEarned: Long = 0,
    @SerialName("pet_clicks") val petClicks: Int = 0,

    // Metadata
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("last_login") val lastLogin: String? = null
)