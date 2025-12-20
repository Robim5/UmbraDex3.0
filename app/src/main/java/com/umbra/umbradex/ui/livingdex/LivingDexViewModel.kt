package com.umbra.umbradex.ui.livingdex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.model.PokemonWithUserData
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.ui.pokedex.SortOrder
import com.umbra.umbradex.ui.pokedex.PokedexFilters
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LivingDexFilters(
    val searchQuery: String = "",
    val selectedType: String? = null,
    val selectedGeneration: Int? = null,
    val sortOrder: SortOrder = SortOrder.NUMBER_ASC,
    val showOwnedOnly: Boolean = false
)

data class TypeCount(
    val type: String,
    val count: Int
)

data class LivingDexUiState(
    val isLoading: Boolean = true,
    val allPokemon: List<Pokemon> = emptyList(),
    val displayedPokemon: List<PokemonWithUserData> = emptyList(),
    val ownedPokemonIds: Set<Int> = emptySet(),
    val filters: LivingDexFilters = LivingDexFilters(),
    val stats: LivingDexStats? = null,
    val topTypes: List<TypeCount> = emptyList(),
    val error: String? = null,
    val showFilters: Boolean = false
)

class LivingDexViewModel(
    private val authRepository: AuthRepository,
    private val pokemonRepository: PokemonRepository,
    private val livingDexRepository: LivingDexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LivingDexUiState())
    val uiState: StateFlow<LivingDexUiState> = _uiState.asStateFlow()

    init {
        loadLivingDex()
    }

    fun loadLivingDex() {
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

            // Load all Pokemon
            when (val pokemonResult = pokemonRepository.getAllPokemon()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        allPokemon = pokemonResult.data ?: emptyList()
                    )

                    // Load owned pokemon
                    loadOwnedPokemon(userId)
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

    private suspend fun loadOwnedPokemon(userId: String) {
        when (val ownedResult = livingDexRepository.getUserLivingDex(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    ownedPokemonIds = ownedResult.data?.toSet() ?: emptySet()
                )

                // Load statistics
                loadStatistics(userId)
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = ownedResult.message
                )
            }
            is NetworkResult.Loading -> {}
        }
    }

    private suspend fun loadStatistics(userId: String) {
        when (val statsResult = livingDexRepository.getLivingDexStats(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    stats = statsResult.data,
                    isLoading = false
                )

                // Calculate top types
                calculateTopTypes()
                applyFilters()
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
                applyFilters()
            }
            is NetworkResult.Loading -> {}
        }
    }

    private fun calculateTopTypes() {
        val ownedPokemon = _uiState.value.allPokemon.filter {
            _uiState.value.ownedPokemonIds.contains(it.nationalNumber)
        }

        val typeCounts = mutableMapOf<String, Int>()

        ownedPokemon.forEach { pokemon ->
            typeCounts[pokemon.typePrimary] = typeCounts.getOrDefault(pokemon.typePrimary, 0) + 1
            pokemon.typeSecondary?.let { secondary ->
                typeCounts[secondary] = typeCounts.getOrDefault(secondary, 0) + 1
            }
        }

        val topTypes = typeCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { TypeCount(it.key, it.value) }

        _uiState.value = _uiState.value.copy(topTypes = topTypes)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.copy(searchQuery = query)
        )
        applyFilters()
    }

    fun selectType(type: String?) {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.copy(selectedType = type)
        )
        applyFilters()
    }

    fun selectGeneration(generation: Int?) {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.copy(selectedGeneration = generation)
        )
        applyFilters()
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.copy(sortOrder = sortOrder)
        )
        applyFilters()
    }

    fun toggleOwnedOnly() {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.copy(
                showOwnedOnly = !_uiState.value.filters.showOwnedOnly
            )
        )
        applyFilters()
    }

    fun toggleFiltersVisibility() {
        _uiState.value = _uiState.value.copy(
            showFilters = !_uiState.value.showFilters
        )
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            filters = LivingDexFilters()
        )
        applyFilters()
    }

    private fun applyFilters() {
        val filters = _uiState.value.filters
        var filtered = _uiState.value.allPokemon

        // Search query
        if (filters.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(filters.searchQuery, ignoreCase = true) ||
                        it.nationalNumber.toString().contains(filters.searchQuery)
            }
        }

        // Type filter
        filters.selectedType?.let { type ->
            filtered = filtered.filter {
                it.typePrimary.equals(type, ignoreCase = true) ||
                        it.typeSecondary?.equals(type, ignoreCase = true) == true
            }
        }

        // Generation filter
        filters.selectedGeneration?.let { gen ->
            filtered = filtered.filter { it.generation == gen }
        }

        // Owned filter
        if (filters.showOwnedOnly) {
            filtered = filtered.filter {
                _uiState.value.ownedPokemonIds.contains(it.nationalNumber)
            }
        }

        // Sort
        filtered = when (filters.sortOrder) {
            SortOrder.NUMBER_ASC -> filtered.sortedBy { it.nationalNumber }
            SortOrder.NUMBER_DESC -> filtered.sortedByDescending { it.nationalNumber }
            SortOrder.NAME_AZ -> filtered.sortedBy { it.name }
            SortOrder.NAME_ZA -> filtered.sortedByDescending { it.name }
        }

        // Map to PokemonWithUserData
        val displayedPokemon = filtered.map { pokemon ->
            PokemonWithUserData(
                pokemon = pokemon,
                isOwned = _uiState.value.ownedPokemonIds.contains(pokemon.nationalNumber)
            )
        }

        _uiState.value = _uiState.value.copy(displayedPokemon = displayedPokemon)
    }

    fun toggleOwned(pokemonId: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            val isOwned = _uiState.value.ownedPokemonIds.contains(pokemonId)

            if (isOwned) {
                when (livingDexRepository.removeFromLivingDex(userId, pokemonId)) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            ownedPokemonIds = _uiState.value.ownedPokemonIds - pokemonId
                        )
                        // Reload stats
                        loadStatistics(userId)
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
                            ownedPokemonIds = _uiState.value.ownedPokemonIds + pokemonId
                        )
                        // Reload stats
                        loadStatistics(userId)
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

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}