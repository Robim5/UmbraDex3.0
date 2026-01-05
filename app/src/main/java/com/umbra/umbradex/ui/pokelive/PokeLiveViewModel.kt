package com.umbra.umbradex.ui.pokelive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.repository.DataRepository
import com.umbra.umbradex.data.repository.PokemonRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokeLiveViewModel : ViewModel() {
    private val repository = PokemonRepository()

    // Cache completo (carregado uma vez)
    private var allPokemon = listOf<Pokemon>()

    // Estado da box atual
    private val _boxState = MutableStateFlow<BoxUiState>(BoxUiState.Loading)
    val boxState = _boxState.asStateFlow()

    // Estado das estatísticas
    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val statsState = _statsState.asStateFlow()

    // Controlos
    private val _currentBoxIndex = MutableStateFlow(1) // Box 1-50
    val currentBoxIndex = _currentBoxIndex.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOption.NUMBER)
    val sortOrder = _sortOrder.asStateFlow()

    init {
        loadAllPokemon()
    }

    // Carrega todos os pokémon UMA VEZ (cache)
    private fun loadAllPokemon() {
        viewModelScope.launch {
            repository.getAllPokemon(limit = 1025).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        allPokemon = result.data
                        loadCurrentBox() // Carrega box 1
                        calculateStats() // Calcula stats globais
                    }
                    is Resource.Error -> {
                        _boxState.value = BoxUiState.Error(result.message)
                        _statsState.value = StatsUiState.Error(result.message)
                    }
                    is Resource.Loading -> {
                        _boxState.value = BoxUiState.Loading
                    }
                }
            }
        }
    }

    // Carrega apenas os 30 pokémon da box atual
    private fun loadCurrentBox() {
        val offset = (_currentBoxIndex.value - 1) * 30
        val boxPokemon = allPokemon
            .drop(offset)
            .take(30)
            .let { sortList(it, _sortOrder.value) }

        _boxState.value = BoxUiState.Success(boxPokemon)
    }

    // Ordenação
    private fun sortList(list: List<Pokemon>, order: SortOption): List<Pokemon> {
        return when (order) {
            SortOption.NUMBER -> list.sortedBy { it.id }
            SortOption.NAME_ASC -> list.sortedBy { it.name }
            SortOption.NAME_DESC -> list.sortedByDescending { it.name }
            SortOption.TYPE -> list.sortedBy { it.types.firstOrNull() ?: "" }
        }
    }

    // Estatísticas globais
    private fun calculateStats() {
        val caught = allPokemon.filter { it.isCaught }
        val totalCaught = caught.size
        val totalMissing = 1025 - totalCaught

        // Top 3 tipos mais comuns
        val typeCount = caught
            .flatMap { it.types }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        _statsState.value = StatsUiState.Success(
            totalCaught = totalCaught,
            totalMissing = totalMissing,
            topTypes = typeCount
        )
    }

    // Navegação de boxes
    fun nextBox() {
        if (_currentBoxIndex.value < 50) {
            _currentBoxIndex.value += 1
            loadCurrentBox()
        }
    }

    fun prevBox() {
        if (_currentBoxIndex.value > 1) {
            _currentBoxIndex.value -= 1
            loadCurrentBox()
        }
    }

    fun goToBox(boxNumber: Int) {
        if (boxNumber in 1..50) {
            _currentBoxIndex.value = boxNumber
            loadCurrentBox()
        }
    }

    // Ordenação
    fun toggleSort() {
        _sortOrder.value = when (_sortOrder.value) {
            SortOption.NUMBER -> SortOption.NAME_ASC
            SortOption.NAME_ASC -> SortOption.NAME_DESC
            SortOption.NAME_DESC -> SortOption.TYPE
            SortOption.TYPE -> SortOption.NUMBER
        }
        loadCurrentBox()
    }

    // Refresh (após adicionar/remover pokémon)
    fun refresh() {
        loadAllPokemon()
    }
}

// Estados
sealed class BoxUiState {
    object Loading : BoxUiState()
    data class Success(val pokemonList: List<Pokemon>) : BoxUiState()
    data class Error(val message: String) : BoxUiState()
}

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(
        val totalCaught: Int,
        val totalMissing: Int,
        val topTypes: List<Pair<String, Int>>
    ) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

enum class SortOption {
    NUMBER,
    NAME_ASC,
    NAME_DESC,
    TYPE
}