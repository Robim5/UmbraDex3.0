package com.umbra.umbradex.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.model.Team
import com.umbra.umbradex.data.model.User
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.util.AudioPlayer
import com.umbra.umbradex.util.Constants
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val equippedPokemon: Pokemon? = null,
    val teams: List<Team> = emptyList(),
    val error: String? = null,
    val petMessage: String? = null,
    val showPetMessage: Boolean = false
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val pokemonRepository: PokemonRepository,
    private val teamRepository: TeamRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val audioPlayer = AudioPlayer(context)

    init {
        loadHomeData()
        playWelcomeSound()
    }

    private fun playWelcomeSound() {
        viewModelScope.launch {
            audioPlayer.playSound(Constants.AUDIO_OPEN_DASHBOARD)
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }

            // Load user profile
            when (val userResult = userRepository.getUserProfile(userId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(user = userResult.data)

                    // Load equipped pokemon if exists
                    userResult.data?.equippedStarter?.let { pokemonId ->
                        loadEquippedPokemon(pokemonId)
                    }

                    // Load teams
                    loadTeams(userId)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = userResult.message
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private suspend fun loadEquippedPokemon(pokemonId: Int) {
        when (val pokemonResult = pokemonRepository.getPokemonById(pokemonId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    equippedPokemon = pokemonResult.data,
                    isLoading = false
                )
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = pokemonResult.message
                )
            }
            is NetworkResult.Loading -> {}
        }
    }

    private suspend fun loadTeams(userId: String) {
        when (val teamsResult = teamRepository.getUserTeams(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    teams = teamsResult.data ?: emptyList()
                )
            }
            is NetworkResult.Error -> {
                // Don't show error for teams, just log it
                println("Failed to load teams: ${teamsResult.message}")
            }
            is NetworkResult.Loading -> {}
        }
    }

    fun onPetClick() {
        viewModelScope.launch {
            val messages = listOf(
                "Pika Pika! ⚡",
                "I love you, trainer! 💖",
                "Let's catch them all! 🎯",
                "Ready for adventure! 🌟",
                "You're the best! ✨",
                "Feed me berries! 🍓",
                "Let's battle! ⚔️",
                "I'm so happy! 😊"
            )

            _uiState.value = _uiState.value.copy(
                petMessage = messages.random(),
                showPetMessage = true
            )

            audioPlayer.playSound(Constants.AUDIO_GOOD_ANIMAL)

            // Hide message after 3 seconds
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(showPetMessage = false)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}