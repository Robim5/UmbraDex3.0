package com.umbra.umbradex.ui.pokedex

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.umbra.umbradex.data.model.EvolutionChainItem
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.model.PokemonStats
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.util.AudioPlayer
import com.umbra.umbradex.util.Constants
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PokemonDetailUiState(
    val isLoading: Boolean = true,
    val pokemon: Pokemon? = null,
    val stats: PokemonStats? = null,
    val evolutionChain: List<EvolutionChainItem> = emptyList(),
    val isFavorite: Boolean = false,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: String? = null
)

class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val authRepository: AuthRepository,
    private val pokemonRepository: PokemonRepository,
    private val favoritesRepository: FavoritesRepository,
    private val livingDexRepository: LivingDexRepository,
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    private val audioPlayer = AudioPlayer(context)
    private val gson = Gson()

    init {
        loadPokemonDetail()
    }

    private fun loadPokemonDetail() {
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

            // Load Pokemon
            when (val pokemonResult = pokemonRepository.getPokemonById(pokemonId)) {
                is NetworkResult.Success -> {
                    val pokemon = pokemonResult.data!!

                    // Parse stats
                    val stats = PokemonStats(
                        hp = pokemon.statsHp ?: 0,
                        attack = pokemon.statsAttack ?: 0,
                        defense = pokemon.statsDefense ?: 0,
                        spAttack = pokemon.statsSpAttack ?: 0,
                        spDefense = pokemon.statsSpDefense ?: 0,
                        speed = pokemon.statsSpeed ?: 0
                    )

                    // Parse evolution chain
                    val evolutionChain = try {
                        pokemon.evolutionChain?.let {
                            gson.fromJson(it, Array<EvolutionChainItem>::class.java).toList()
                        } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    _uiState.value = _uiState.value.copy(
                        pokemon = pokemon,
                        stats = stats,
                        evolutionChain = evolutionChain
                    )

                    // Load user data
                    loadUserData(userId, pokemonId)
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
    }

    private suspend fun loadUserData(userId: String, pokemonId: Int) {
        // Check if favorite
        when (val favoriteResult = favoritesRepository.isFavorite(userId, pokemonId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isFavorite = favoriteResult.data ?: false
                )
            }
            is NetworkResult.Error -> {
                println("Failed to check favorite: ${favoriteResult.message}")
            }
            is NetworkResult.Loading -> {}
        }

        // Check if owned
        when (val ownedResult = livingDexRepository.ownsPokem(userId, pokemonId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isOwned = ownedResult.data ?: false
                )
            }
            is NetworkResult.Error -> {
                println("Failed to check owned: ${ownedResult.message}")
            }
            is NetworkResult.Loading -> {}
        }

        // Check if equipped
        when (val userResult = userRepository.getUserProfile(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isEquipped = userResult.data?.equippedStarter == pokemonId,
                    isLoading = false
                )
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
            is NetworkResult.Loading -> {}
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            if (_uiState.value.isFavorite) {
                when (favoritesRepository.removeFromFavorites(userId, pokemonId)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isFavorite = false,
                            showSuccessMessage = "Removed from favorites"
                        )
                        audioPlayer.playSound(Constants.AUDIO_EQUIP_BADGE)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to remove from favorites"
                        )
                    }
                    is NetworkResult.Loading -> {}
                }
            } else {
                when (favoritesRepository.addToFavorites(userId, pokemonId)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isFavorite = true,
                            showSuccessMessage = "Added to favorites"
                        )
                        audioPlayer.playSound(Constants.AUDIO_EQUIP_BADGE)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to add to favorites"
                        )
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun toggleOwned() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            if (_uiState.value.isOwned) {
                when (livingDexRepository.removeFromLivingDex(userId, pokemonId)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isOwned = false,
                            showSuccessMessage = "Removed from Living Dex"
                        )
                        audioPlayer.playSound(Constants.AUDIO_EQUIP_BADGE)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to remove from Living Dex"
                        )
                    }
                    is NetworkResult.Loading -> {}
                }
            } else {
                when (livingDexRepository.addToLivingDex(userId, pokemonId)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isOwned = true,
                            showSuccessMessage = "Added to Living Dex"
                        )
                        audioPlayer.playSound(Constants.AUDIO_GET_SOMETHING)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to add to Living Dex"
                        )
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun equipPokemon() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            when (userRepository.updateEquippedPokemon(userId, pokemonId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isEquipped = true,
                        showSuccessMessage = "Equipped as partner!"
                    )
                    audioPlayer.playSound(Constants.AUDIO_EQUIP_BADGE)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to equip Pokémon"
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun playCry() {
        viewModelScope.launch {
            _uiState.value.pokemon?.cryUrl?.let { cryUrl ->
                // Note: Playing remote audio would require ExoPlayer
                // For now, play a local sound
                audioPlayer.playSound(Constants.AUDIO_GOOD_ANIMAL)
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(
            error = null,
            showSuccessMessage = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}