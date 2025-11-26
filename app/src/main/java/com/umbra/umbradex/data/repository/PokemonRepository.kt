package com.umbra.umbradex.data.repository

import com.google.gson.Gson
import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.EvolutionChainItem
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.remote.pokeapi.PokeApiClient
import com.umbra.umbradex.util.Constants
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PokemonRepository {

    private val supabase = UmbraDexApplication.supabase
    private val pokeApi = PokeApiClient.service
    private val gson = Gson()

    // Fetch all Pokemon from Supabase
    suspend fun getAllPokemon(): NetworkResult<List<Pokemon>> {
        return try {
            val result = supabase.from("pokemon")
                .select()
                .decodeList<Pokemon>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch pokemon")
        }
    }

    // Get Pokemon by ID from Supabase
    suspend fun getPokemonById(id: Int): NetworkResult<Pokemon> {
        return try {
            val result = supabase.from("pokemon")
                .select {
                    filter {
                        eq("national_number", id)
                    }
                }
                .decodeSingle<Pokemon>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch pokemon")
        }
    }

    // Filter Pokemon by generation
    suspend fun getPokemonByGeneration(generation: Int): NetworkResult<List<Pokemon>> {
        return try {
            val result = supabase.from("pokemon")
                .select {
                    filter {
                        eq("generation", generation)
                    }
                }
                .decodeList<Pokemon>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch pokemon by generation")
        }
    }

    // Filter Pokemon by type
    suspend fun getPokemonByType(type: String): NetworkResult<List<Pokemon>> {
        return try {
            val result = supabase.from("pokemon")
                .select {
                    filter {
                        or {
                            eq("type_primary", type)
                            eq("type_secondary", type)
                        }
                    }
                }
                .decodeList<Pokemon>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch pokemon by type")
        }
    }

    // Search Pokemon by name
    suspend fun searchPokemon(query: String): NetworkResult<List<Pokemon>> {
        return try {
            val result = supabase.from("pokemon")
                .select {
                    filter {
                        ilike("name", "%$query%")
                    }
                }
                .decodeList<Pokemon>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to search pokemon")
        }
    }

    // Fetch Pokemon from PokéAPI and save to Supabase (for seeding database)
    suspend fun fetchAndSavePokemon(nationalNumber: Int): NetworkResult<Pokemon> {
        return try {
            // Fetch from PokéAPI
            val pokemonResponse = pokeApi.getPokemon(nationalNumber)
            val speciesResponse = pokeApi.getPokemonSpecies(nationalNumber)

            if (!pokemonResponse.isSuccessful || !speciesResponse.isSuccessful) {
                return NetworkResult.Error("Failed to fetch from PokéAPI")
            }

            val pokemonData = pokemonResponse.body()!!
            val speciesData = speciesResponse.body()!!

            // Extract generation number from generation name (e.g., "generation-i" -> 1)
            val generation = when (speciesData.generation.name) {
                "generation-i" -> 1
                "generation-ii" -> 2
                "generation-iii" -> 3
                "generation-iv" -> 4
                "generation-v" -> 5
                "generation-vi" -> 6
                "generation-vii" -> 7
                "generation-viii" -> 8
                "generation-ix" -> 9
                else -> 1
            }

            // Get evolution chain (simplified)
            val evolutionChainId = speciesData.evolutionChain.url.split("/").dropLast(1).last().toInt()
            val evolutionResponse = pokeApi.getEvolutionChain(evolutionChainId)
            val evolutionChain = if (evolutionResponse.isSuccessful) {
                parseEvolutionChain(evolutionResponse.body()!!)
            } else null

            // Build Pokemon object
            val pokemon = buildJsonObject {
                put("national_number", pokemonData.id)
                put("name", pokemonData.name)
                put("generation", generation)
                put("type_primary", pokemonData.types.first { it.slot == 1 }.type.name)
                pokemonData.types.firstOrNull { it.slot == 2 }?.let {
                    put("type_secondary", it.type.name)
                }
                put("sprite_url", pokemonData.sprites.other?.officialArtwork?.frontDefault
                    ?: pokemonData.sprites.frontDefault)
                put("cry_url", pokemonData.cries?.latest ?: pokemonData.cries?.legacy)

                // Stats
                pokemonData.stats.forEach { stat ->
                    when (stat.stat.name) {
                        "hp" -> put("stats_hp", stat.baseStat)
                        "attack" -> put("stats_attack", stat.baseStat)
                        "defense" -> put("stats_defense", stat.baseStat)
                        "special-attack" -> put("stats_sp_attack", stat.baseStat)
                        "special-defense" -> put("stats_sp_defense", stat.baseStat)
                        "speed" -> put("stats_speed", stat.baseStat)
                    }
                }

                // Evolution chain as JSON
                evolutionChain?.let {
                    put("evolution_chain", gson.toJson(it))
                }
            }

            // Save to Supabase
            val result = supabase.from("pokemon")
                .insert(pokemon)
                .decodeSingle<Pokemon>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch and save pokemon")
        }
    }

    // Helper: Parse evolution chain
    private fun parseEvolutionChain(response: com.umbra.umbradex.data.remote.pokeapi.PokeApiEvolutionChainResponse): List<EvolutionChainItem> {
        val chain = mutableListOf<EvolutionChainItem>()

        fun traverse(link: com.umbra.umbradex.data.remote.pokeapi.ChainLink) {
            val id = link.species.url.split("/").dropLast(1).last().toInt()
            val minLevel = link.evolutionDetails?.firstOrNull()?.minLevel
            val trigger = link.evolutionDetails?.firstOrNull()?.trigger?.name

            chain.add(EvolutionChainItem(
                id = id,
                name = link.species.name,
                minLevel = minLevel,
                trigger = trigger
            ))

            link.evolvesTo?.forEach { traverse(it) }
        }

        traverse(response.chain)
        return chain
    }
}