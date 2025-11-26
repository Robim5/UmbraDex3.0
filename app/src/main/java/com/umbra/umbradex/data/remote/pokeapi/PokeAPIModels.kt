package com.umbra.umbradex.data.remote.pokeapi

import com.google.gson.annotations.SerializedName

// Response from PokéAPI /pokemon/{id}
data class PokeApiPokemonResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>,
    val cries: Cries?
)

data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String?,
    val other: OtherSprites?
)

data class OtherSprites(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtwork?
)

data class OfficialArtwork(
    @SerializedName("front_default")
    val frontDefault: String?
)

data class TypeSlot(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String,
    val url: String
)

data class StatSlot(
    @SerializedName("base_stat")
    val baseStat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)

data class Cries(
    val latest: String?,
    val legacy: String?
)

// Response from PokéAPI /pokemon-species/{id}
data class PokeApiSpeciesResponse(
    val id: Int,
    val name: String,
    val generation: Generation,
    @SerializedName("evolution_chain")
    val evolutionChain: EvolutionChainUrl
)

data class Generation(
    val name: String,
    val url: String
)

data class EvolutionChainUrl(
    val url: String
)

// Response from PokéAPI /evolution-chain/{id}
data class PokeApiEvolutionChainResponse(
    val chain: ChainLink
)

data class ChainLink(
    val species: SpeciesReference,
    @SerializedName("evolves_to")
    val evolvesTo: List<ChainLink>?,
    @SerializedName("evolution_details")
    val evolutionDetails: List<EvolutionDetail>?
)

data class SpeciesReference(
    val name: String,
    val url: String
)

data class EvolutionDetail(
    @SerializedName("min_level")
    val minLevel: Int?,
    val trigger: Trigger?
)

data class Trigger(
    val name: String
)