package com.umbra.umbradex.data.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val shinyImageUrl: String? = null,           // ← ADICIONA
    val types: List<String>,
    val weight: Double,
    val height: Double,
    val description: String,
    val stats: List<PokemonStat>,
    val evolutions: List<EvolutionStep>,
    val isCaught: Boolean,
    val isFavorite: Boolean,
    val abilities: List<String> = emptyList(),   // ← ADICIONA
    val cryUrl: String? = null,                  // ← ADICIONA
    val isLegendary: Boolean = false,            // ← ADICIONA
    val isMythical: Boolean = false              // ← ADICIONA
)

data class PokemonStat(
    val name: String,
    val value: Int,
    val max: Int = 255
)

data class EvolutionStep(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val evolutionTrigger: String = ""
)