package com.umbra.umbradex.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.model.PokemonWithUserData
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.util.Constants
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOrder {
    NUMBER_ASC,
    NUMBER_DESC,
    NAME_AZ,
    NAME_ZA
}

data class PokedexFilters(
    val searchQuery: String = "",
    val selectedType: String? = null,
    val selectedGeneration: Int? = null,
    val sortOrder: SortOrder = SortOrder.NUMBER_ASC,
    val showFavoritesOnly: Boolean = false
)

data class PokedexUiState(
    val isLoading: Boolean = true,
    val allPokemon: List<Pokemon> = emptyList(),
    val displayedPokemon: List<PokemonWithUserData> = emptyList(),
    val favoritePokemonIds: Set<Int> = emptySet(),
    val ownedPokemonIds: Set<Int> = emptySet(),
    val filters: PokedexFilters = PokedexFilters(),
    val error: String? = null,
    val showFilters: Boolean = false
)

class PokedexViewModel(
    private val authRepository: AuthRepository,
    private val pokemonRepository: PokemonRepository,
    private val favoritesRepository: FavoritesRepository,
    private val livingDexRepository: LivingDexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        loadPokedex()
    }

    fun loadPokedex() {
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

                    // Load user favorites
                    loadFavorites(userId)
                    // Load user owned pokemon
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

    private suspend fun loadFavorites(userId: String) {
        when (val favoritesResult = favoritesRepository.getUserFavorites(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    favoritePokemonIds = favoritesResult.data?.toSet() ?: emptySet()
                )
                applyFilters()
            }
            is NetworkResult.Error -> {
                println("Failed to load favorites: ${favoritesResult.message}")
                applyFilters()
            }
            is NetworkResult.Loading -> {}
        }
    }

    private suspend fun loadOwnedPokemon(userId: String) {
        when (val ownedResult = livingDexRepository.getUserLivingDex(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    ownedPokemonIds = ownedResult.data?.toSet() ?: emptySet(),
                    isLoading = false
                )
                applyFilters()
            }
            is NetworkResult.Error -> {
                println("Failed to load owned pokemon: ${ownedResult.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
                applyFilters()
            }
            is NetworkResult.Loading -> {}
        }
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

    fun toggleFavoritesOnly() {
        _uiState.value = _uiState.value.copy(
            filters = _uiState.value.filters.copy(
                showFavoritesOnly = !_uiState.value.filters.showFavoritesOnly
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
            filters = PokedexFilters()
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

        // Favorites filter
        if (filters.showFavoritesOnly) {
            filtered = filtered.filter {
                _uiState.value.favoritePokemonIds.contains(it.nationalNumber)
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
                isFavorite = _uiState.value.favoritePokemonIds.contains(pokemon.nationalNumber),
                isOwned = _uiState.value.ownedPokemonIds.contains(pokemon.nationalNumber)
            )
        }

        _uiState.value = _uiState.value.copy(displayedPokemon = displayedPokemon)
    }

    fun toggleFavorite(pokemonId: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            val isFavorite = _uiState.value.favoritePokemonIds.contains(pokemonId)

            if (isFavorite) {
                favoritesRepository.removeFromFavorites(userId, pokemonId)
                _uiState.value = _uiState.value.copy(
                    favoritePokemonIds = _uiState.value.favoritePokemonIds - pokemonId
                )
            } else {
                favoritesRepository.addToFavorites(userId, pokemonId)
                _uiState.value = _uiState.value.copy(
                    favoritePokemonIds = _uiState.value.favoritePokemonIds + pokemonId
                )
            }

            applyFilters()
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}