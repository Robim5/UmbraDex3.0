package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val region: String? = null,
    @SerialName("pokemon_ids")
    val pokemonIds: List<Int> = emptyList(),
    @SerialName("created_at")
    val createdAt: String? = null
)

// For displaying teams with full Pokemon data
data class TeamWithPokemon(
    val team: Team,
    val pokemon: List<Pokemon>
)