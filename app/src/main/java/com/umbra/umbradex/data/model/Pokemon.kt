package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val id: Int,
    @SerialName("national_number")
    val nationalNumber: Int,
    val name: String,
    val generation: Int,
    @SerialName("type_primary")
    val typePrimary: String,
    @SerialName("type_secondary")
    val typeSecondary: String? = null,
    @SerialName("sprite_url")
    val spriteUrl: String? = null,
    @SerialName("cry_url")
    val cryUrl: String? = null,
    @SerialName("stats_hp")
    val statsHp: Int? = null,
    @SerialName("stats_attack")
    val statsAttack: Int? = null,
    @SerialName("stats_defense")
    val statsDefense: Int? = null,
    @SerialName("stats_sp_attack")
    val statsSpAttack: Int? = null,
    @SerialName("stats_sp_defense")
    val statsSpDefense: Int? = null,
    @SerialName("stats_speed")
    val statsSpeed: Int? = null,
    @SerialName("evolution_chain")
    val evolutionChain: String? = null // JSON string
)

// For displaying Pokemon with user data
data class PokemonWithUserData(
    val pokemon: Pokemon,
    val isFavorite: Boolean = false,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false
)

// Pokemon Stats wrapper
data class PokemonStats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val spAttack: Int,
    val spDefense: Int,
    val speed: Int
) {
    val total: Int
        get() = hp + attack + defense + spAttack + spDefense + speed
}

// Evolution chain item
@Serializable
data class EvolutionChainItem(
    val id: Int,
    val name: String,
    val minLevel: Int? = null,
    val trigger: String? = null // "level", "stone", "trade", etc.
)