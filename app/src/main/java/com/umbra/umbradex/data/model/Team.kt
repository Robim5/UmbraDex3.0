package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val region: String,
    @SerialName("gradient_colors") val gradientColors: List<String>, // Lista de cores hex para o gradiente
    @SerialName("created_at") val createdAt: String,
    val pokemon: List<TeamPokemon> = emptyList() // Máximo 6
) {
    @Serializable
    data class TeamPokemon(
        @SerialName("pokemon_id") val pokemonId: Int,
        val name: String,
        @SerialName("image_url") val imageUrl: String,
        val level: Int,
        @SerialName("slot_index") val slotIndex: Int, // 0 a 5
        val types: List<String> = emptyList()
    ) {
        val id: Int get() = pokemonId
    }

    //Verifica se a equipa está cheia (6 Pokémon)
    fun isFull(): Boolean = pokemon.size >= 6

    /**
     * Verifica se a equipa está vazia
     */
    fun isEmpty(): Boolean = pokemon.isEmpty()

    /**
     * Retorna o número de slots vazios
     */
    fun emptySlots(): Int = 6 - pokemon.size

    /**
     * Verifica se um Pokémon já está na equipa
     */
    fun hasPokemon(pokemonId: Int): Boolean {
        return pokemon.any { it.pokemonId == pokemonId }
    }

    /**
     * Retorna o Pokémon num slot específico (0-5)
     */
    fun getPokemonInSlot(slotIndex: Int): TeamPokemon? {
        return pokemon.find { it.slotIndex == slotIndex }
    }
}

/**
 * DTO para criar uma nova equipa
 */
@Serializable
data class CreateTeamDto(
    val name: String,
    val region: String,
    @SerialName("gradient_colors") val gradientColors: List<String>
)

/**
 * DTO para adicionar/substituir um Pokémon numa equipa
 */
@Serializable
data class AddPokemonToTeamDto(
    @SerialName("team_id") val teamId: String,
    @SerialName("pokemon_id") val pokemonId: Int,
    val level: Int,
    @SerialName("slot_index") val slotIndex: Int
)

/**
 * DTO para atualizar o nome de uma equipa
 */
@Serializable
data class UpdateTeamNameDto(
    @SerialName("team_id") val teamId: String,
    val name: String
)