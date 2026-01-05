package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.api.RetrofitClient
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.model.UserPokemon
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.umbra.umbradex.data.model.*
import kotlinx.coroutines.flow.flow




class PokemonRepository {

    private val db = UmbraSupabase.db
    private val auth = UmbraSupabase.auth
    private val pokeApi = RetrofitClient.api

    // ========================================
    // MÉTODO EXISTENTE: getAllPokemon
    // ========================================
    suspend fun getAllPokemon(limit: Int = 1025): Flow<Resource<List<Pokemon>>> = flow {
        emit(Resource.Loading)
        try {
            val pokemonList = mutableListOf<Pokemon>()

            val userId = auth.currentUserOrNull()?.id
            val caughtIds = if (userId != null) {
                db.from("user_pokemons")
                    .select(columns = Columns.list("pokedex_id")) {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<UserPokemon>()
                    .map { it.pokedexId }
            } else emptyList()

            val favoriteIds = if (userId != null) {
                db.from("favorites")
                    .select(columns = Columns.list("pokedex_id")) {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<Map<String, Int>>()
                    .map { it["pokedex_id"] ?: 0 }
            } else emptyList()

            for (id in 1..limit) {
                try {
                    val dto = pokeApi.getPokemonDetail(id)

                    pokemonList.add(
                        Pokemon(
                            id = dto.id,
                            name = dto.name,
                            imageUrl = dto.sprites.other?.officialArtwork?.frontDefault
                                ?: dto.sprites.frontDefault ?: "",
                            types = dto.types.map { it.type.name.replaceFirstChar { c -> c.uppercase() } },
                            height = dto.height / 10.0,
                            weight = dto.weight / 10.0,
                            isCaught = caughtIds.contains(dto.id),
                            isFavorite = favoriteIds.contains(dto.id)
                        )
                    )
                } catch (e: Exception) {
                    continue
                }
            }

            emit(Resource.Success(pokemonList))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load Pokémon: ${e.message}"))
        }
    }

    // ========================================
    // NOVO MÉTODO: getPokemonFullDetails
    // ========================================
    suspend fun getPokemonFullDetails(pokemonId: Int): Flow<Resource<PokemonDetail>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id

            // 1. Buscar dados base
            val pokemonDto = pokeApi.getPokemonDetail(pokemonId)

            // 2. Buscar species
            val speciesDto = pokeApi.getPokemonSpecies(pokemonId)

            // 3. Buscar evolution chain
            val evolutionChainDto = pokeApi.getEvolutionChain(speciesDto.evolutionChain.url)

            // 4. Descrição
            val description = speciesDto.flavorTextEntries
                .firstOrNull { it.language.name == "en" }
                ?.flavorText
                ?.replace("\n", " ")
                ?.replace("\u000c", " ")
                ?: "No description available."

            // 5. Stats
            val stats = pokemonDto.stats.map { statDto ->
                PokemonStat(
                    name = when (statDto.stat.name) {
                        "hp" -> "HP"
                        "attack" -> "ATK"
                        "defense" -> "DEF"
                        "special-attack" -> "SP.ATK"
                        "special-defense" -> "SP.DEF"
                        "speed" -> "SPEED"
                        else -> statDto.stat.name.uppercase()
                    },
                    value = statDto.baseStat,
                    max = 255
                )
            }

            // 6. Evolution chain
            val evolutions = parseEvolutionChain(evolutionChainDto.chain)

            // 7. Verificar caught/favorite
            var isCaught = false
            var isFavorite = false

            if (userId != null) {
                isCaught = db.from("user_pokemons")
                    .select(columns = Columns.list("id")) {
                        filter {
                            eq("user_id", userId)
                            eq("pokedex_id", pokemonId)
                        }
                    }
                    .decodeList<Map<String, String>>()
                    .isNotEmpty()

                isFavorite = db.from("favorites")
                    .select(columns = Columns.list("user_id")) {
                        filter {
                            eq("user_id", userId)
                            eq("pokedex_id", pokemonId)
                        }
                    }
                    .decodeList<Map<String, String>>()
                    .isNotEmpty()
            }

            // 8. Construir PokemonDetail
            val detail = PokemonDetail(
                id = pokemonDto.id,
                name = pokemonDto.name,
                imageUrl = pokemonDto.sprites.other?.officialArtwork?.frontDefault ?: "",
                shinyImageUrl = pokemonDto.sprites.other?.officialArtwork?.frontShiny,
                types = pokemonDto.types.map { it.type.name.replaceFirstChar { c -> c.uppercase() } },
                weight = pokemonDto.weight / 10.0,
                height = pokemonDto.height / 10.0,
                description = description,
                stats = stats,
                evolutions = evolutions,
                isCaught = isCaught,
                isFavorite = isFavorite,
                abilities = pokemonDto.abilities.map {
                    it.ability.name.replaceFirstChar { c -> c.uppercase() }
                },
                cryUrl = pokemonDto.cries?.latest,
                isLegendary = speciesDto.isLegendary,
                isMythical = speciesDto.isMythical
            )

            emit(Resource.Success(detail))

        } catch (e: Exception) {
            emit(Resource.Error("Failed to load details: ${e.message}"))
        }
    }

    private fun parseEvolutionChain(chain: ChainLink): List<EvolutionStep> {
        val evolutions = mutableListOf<EvolutionStep>()

        fun traverse(link: ChainLink) {
            val speciesId = link.species.url.split("/").dropLast(1).last().toIntOrNull() ?: 0

            val evolutionTrigger = if (link.evolutionDetails.isNotEmpty()) {
                val detail = link.evolutionDetails.first()
                when {
                    detail.minLevel != null -> "Lv. ${detail.minLevel}"
                    detail.item != null -> detail.item.name.replaceFirstChar { it.uppercase() }
                    detail.trigger.name == "trade" -> "Trade"
                    else -> ""
                }
            } else ""

            evolutions.add(
                EvolutionStep(
                    id = speciesId,
                    name = link.species.name.replaceFirstChar { it.uppercase() },
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$speciesId.png",
                    evolutionTrigger = evolutionTrigger
                )
            )

            link.evolvesTo.forEach { traverse(it) }
        }

        traverse(chain)
        return evolutions
    }

    // ========================================
    // MÉTODOS EXISTENTES (mantém os que já tens)
    // ========================================
    suspend fun addToLivingDex(pokedexId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.from("user_pokemons").insert(
                mapOf(
                    "user_id" to userId,
                    "pokedex_id" to pokedexId
                )
            )

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to add Pokémon: ${e.message}"))
        }
    }

    suspend fun removeFromLivingDex(pokedexId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.from("user_pokemons").delete {
                filter {
                    eq("user_id", userId)
                    eq("pokedex_id", pokedexId)
                }
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to remove Pokémon: ${e.message}"))
        }
    }

    suspend fun addFavorite(pokedexId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.from("favorites").insert(
                mapOf(
                    "user_id" to userId,
                    "pokedex_id" to pokedexId
                )
            )

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to add favorite: ${e.message}"))
        }
    }

    suspend fun removeFavorite(pokedexId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            db.from("favorites").delete {
                filter {
                    eq("user_id", userId)
                    eq("pokedex_id", pokedexId)
                }
            }

            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to remove favorite: ${e.message}"))
        }
    }

    suspend fun getUserLivingDex(): Flow<Resource<List<UserPokemon>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            val userPokemons = db.from("user_pokemons")
                .select()
                {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserPokemon>()

            emit(Resource.Success(userPokemons))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load Living Dex: ${e.message}"))
        }
    }
}