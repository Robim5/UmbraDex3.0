package com.umbra.umbradex.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.EvolutionStep
import com.umbra.umbradex.data.model.PokemonDetail
import com.umbra.umbradex.data.model.PokemonStat
import com.umbra.umbradex.data.repository.DataRepository
import com.umbra.umbradex.data.repository.PokemonRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonDetailViewModel : ViewModel() {
    private val repo = PokemonRepository()

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _showShiny = MutableStateFlow(false)
    val showShiny = _showShiny.asStateFlow()

    fun loadPokemon(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading

            repo.getPokemonFullDetails(id).collect { result ->
                _uiState.value = when (result) {
                    is Resource.Success -> DetailUiState.Success(result.data)
                    is Resource.Error -> DetailUiState.Error(result.message)
                    is Resource.Loading -> DetailUiState.Loading
                }
            }
        }
    }

    fun toggleShiny() {
        _showShiny.value = !_showShiny.value
    }

    fun toggleCatch() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                val pokemon = currentState.data

                if (pokemon.isCaught) {
                    repo.removeFromLivingDex(pokemon.id)
                } else {
                    repo.addToLivingDex(pokemon.id)
                }

                // Atualiza o estado local
                _uiState.value = DetailUiState.Success(
                    pokemon.copy(isCaught = !pokemon.isCaught)
                )
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                val pokemon = currentState.data

                if (pokemon.isFavorite) {
                    repo.removeFavorite(pokemon.id)
                } else {
                    repo.addFavorite(pokemon.id)
                }

                _uiState.value = DetailUiState.Success(
                    pokemon.copy(isFavorite = !pokemon.isFavorite)
                )
            }
        }
    }
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val data: PokemonDetail) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}