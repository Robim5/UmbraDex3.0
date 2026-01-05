package com.umbra.umbradex.ui.teams

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.Team
import com.umbra.umbradex.data.repository.TeamRepository
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.repository.PokemonRepository
import com.umbra.umbradex.utils.ImageUtils
import kotlinx.coroutines.flow.StateFlow


class TeamsViewModel(
    private val teamRepository: TeamRepository = TeamRepository()
) : ViewModel() {

    // Estados das equipas
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTeams()
    }

    /**
     * Carrega todas as equipas do utilizador
     */
    private fun loadTeams() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            teamRepository.getUserTeams().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _teams.value = resource.data.sortedBy { it.createdAt }
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                        _isLoading.value = false
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }

    /**
     * Cria uma nova equipa com cores gradiente aleatórias
     */
    fun createTeam(name: String, region: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Gerar cores aleatórias para o gradiente
                val gradientColors = generateRandomGradient()

                teamRepository.createTeam(
                    name = name,
                    region = region,
                    gradientColors = gradientColors
                )

                loadTeams() // Recarrega as equipas
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create team"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Adiciona ou substitui um Pokémon num slot específico da equipa
     */
    fun addOrReplacePokemonInTeam(
        teamId: String,
        slotIndex: Int,
        pokemon: Pokemon,
        level: Int
    ) {
        viewModelScope.launch {
            try {
                teamRepository.addOrReplacePokemonInTeam(
                    teamId = teamId,
                    slotIndex = slotIndex,
                    pokemonId = pokemon.id,
                    pokemonName = pokemon.name,
                    pokemonImageUrl = pokemon.imageUrl,
                    pokemonTypes = pokemon.types,
                    level = level
                )

                loadTeams() // Recarrega as equipas
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add Pokémon to team"
            }
        }
    }

    /**
     * Remove um Pokémon de um slot específico
     */
    fun removePokemonFromSlot(teamId: String, slotIndex: Int) {
        viewModelScope.launch {
            try {
                teamRepository.removePokemonFromSlot(
                    teamId = teamId,
                    slotIndex = slotIndex
                )

                loadTeams() // Recarrega as equipas
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove Pokémon from team"
            }
        }
    }

    /**
     * Atualiza o nome de uma equipa
     */
    fun updateTeamName(teamId: String, newName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                teamRepository.updateTeamName(
                    teamId = teamId,
                    newName = newName
                )

                loadTeams() // Recarrega as equipas
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update team name"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina uma equipa
     */
    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                teamRepository.deleteTeam(teamId)
                loadTeams() // Recarrega as equipas
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete team"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Faz download do cartão da equipa como imagem PNG
     */
    fun downloadTeamCard(context: Context, team: Team) {
        viewModelScope.launch {
            try {
                // TODO: Implementar a captura da view do TeamCard como Bitmap
                // Por agora, apenas mostra uma mensagem
                Toast.makeText(
                    context,
                    "Downloading team card: ${team.name}",
                    Toast.LENGTH_SHORT
                ).show()

                // A implementação real seria algo como:
                // val bitmap = captureTeamCardAsBitmap(team)
                // ImageUtils.saveBitmapToGallery(context, bitmap, "team_${team.name}")

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error downloading team card: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Limpa a mensagem de erro
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Recarrega os dados
     */
    fun refresh() {
        loadTeams()
    }

    /**
     * Gera um gradiente de 2 cores aleatórias em formato hex
     */
    private fun generateRandomGradient(): List<String> {
        val gradients = listOf(
            // Gradientes predefinidos bonitos
            listOf("#667eea", "#764ba2"), // Roxo
            listOf("#f093fb", "#f5576c"), // Rosa
            listOf("#4facfe", "#00f2fe"), // Azul
            listOf("#43e97b", "#38f9d7"), // Verde
            listOf("#fa709a", "#fee140"), // Laranja-Rosa
            listOf("#30cfd0", "#330867"), // Azul-Roxo
            listOf("#a8edea", "#fed6e3"), // Pastel
            listOf("#ff9a9e", "#fecfef"), // Rosa suave
            listOf("#fbc2eb", "#a6c1ee"), // Lavanda
            listOf("#fdcbf1", "#e6dee9"), // Rosa claro
            listOf("#a1c4fd", "#c2e9fb"), // Azul claro
            listOf("#ffecd2", "#fcb69f"), // Pêssego
            listOf("#ff6e7f", "#bfe9ff"), // Rosa-Azul
            listOf("#e0c3fc", "#8ec5fc"), // Roxo-Azul
            listOf("#f093fb", "#f5576c"), // Magenta
            listOf("#4facfe", "#00f2fe"), // Ciano
            listOf("#43e97b", "#38f9d7"), // Turquesa
            listOf("#fa709a", "#fee140"), // Sunset
            listOf("#30cfd0", "#330867"), // Deep Blue
            listOf("#a8edea", "#fed6e3")  // Cotton Candy
        )

        return gradients[Random.nextInt(gradients.size)]
    }
}