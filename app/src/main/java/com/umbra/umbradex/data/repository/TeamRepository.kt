package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.model.Team
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TeamRepository {

    /**
     * Busca todas as equipas do utilizador com os seus Pokémon
     */
    suspend fun getUserTeams(): Flow<Resource<List<Team>>> = flow {
        emit(Resource.Loading)

        try {
            val userId = UmbraSupabase.client.auth.currentUserOrNull()?.id
                ?: throw Exception("User not logged in")

            // Buscar as equipas do utilizador
            val teamsResponse = UmbraSupabase.client
                .from("teams")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Team>()

            // Para cada equipa, buscar os Pokémon
            val teamsWithPokemon = teamsResponse.map { team ->
                val pokemonResponse = UmbraSupabase.client
                    .from("team_pokemon")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("team_id", team.id)
                        }
                        order("slot_index", Order.ASCENDING)
                    }
                    .decodeList<Team.TeamPokemon>()

                team.copy(pokemon = pokemonResponse)
            }

            emit(Resource.Success(teamsWithPokemon))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load teams: ${e.message}", e))
        }
    }

    /**
     * Cria uma nova equipa
     */
    suspend fun createTeam(
        name: String,
        region: String,
        gradientColors: List<String>
    ) {
        val userId = UmbraSupabase.client.auth.currentUserOrNull()?.id
            ?: throw Exception("User not logged in")

        val teamData = buildJsonObject {
            put("user_id", userId)
            put("name", name)
            put("region", region)
            put("gradient_colors", gradientColors.toString()) // Converte para string JSON array
        }

        UmbraSupabase.client
            .from("teams")
            .insert(teamData)
    }

    /**
     * Adiciona ou substitui um Pokémon num slot específico
     */
    suspend fun addOrReplacePokemonInTeam(
        teamId: String,
        slotIndex: Int,
        pokemonId: Int,
        pokemonName: String,
        pokemonImageUrl: String,
        pokemonTypes: List<String>,
        level: Int
    ) {
        // Primeiro, verifica se já existe um Pokémon nesse slot
        val existingPokemon = UmbraSupabase.client
            .from("team_pokemon")
            .select(columns = Columns.ALL) {
                filter {
                    eq("team_id", teamId)
                    eq("slot_index", slotIndex)
                }
            }
            .decodeSingleOrNull<Team.TeamPokemon>()

        if (existingPokemon != null) {
            // Atualizar o Pokémon existente
            val updateData = buildJsonObject {
                put("pokemon_id", pokemonId)
                put("pokemon_name", pokemonName)
                put("pokemon_image_url", pokemonImageUrl)
                put("pokemon_types", pokemonTypes.toString())
                put("level", level)
            }

            UmbraSupabase.client
                .from("team_pokemon")
                .update(updateData) {
                    filter {
                        eq("team_id", teamId)
                        eq("slot_index", slotIndex)
                    }
                }
        } else {
            // Inserir novo Pokémon
            val insertData = buildJsonObject {
                put("team_id", teamId)
                put("pokemon_id", pokemonId)
                put("pokemon_name", pokemonName)
                put("pokemon_image_url", pokemonImageUrl)
                put("pokemon_types", pokemonTypes.toString())
                put("level", level)
                put("slot_index", slotIndex)
            }

            UmbraSupabase.client
                .from("team_pokemon")
                .insert(insertData)
        }
    }

    /**
     * Remove um Pokémon de um slot específico
     */
    suspend fun removePokemonFromSlot(teamId: String, slotIndex: Int) {
        UmbraSupabase.client
            .from("team_pokemon")
            .delete {
                filter {
                    eq("team_id", teamId)
                    eq("slot_index", slotIndex)
                }
            }
    }

    /**
     * Atualiza o nome de uma equipa
     */
    suspend fun updateTeamName(teamId: String, newName: String) {
        val updateData = buildJsonObject {
            put("name", newName)
        }

        UmbraSupabase.client
            .from("teams")
            .update(updateData) {
                filter {
                    eq("id", teamId)
                }
            }
    }

    /**
     * Elimina uma equipa (os Pokémon são eliminados automaticamente por CASCADE)
     */
    suspend fun deleteTeam(teamId: String) {
        UmbraSupabase.client
            .from("teams")
            .delete {
                filter {
                    eq("id", teamId)
                }
            }
    }
}