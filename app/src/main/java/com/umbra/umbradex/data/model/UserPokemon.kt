package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPokemon(
    val id: String,
    @SerialName("pokedex_id") val pokedexId: Int,
    @SerialName("obtained_at") val obtainedAt: String? = null
)