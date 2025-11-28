package com.umbra.umbradex.ui.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.ui.components.LoadingOverlay
import com.umbra.umbradex.ui.components.ShimmerCard
import com.umbra.umbradex.ui.pokedex.components.FilterBar
import com.umbra.umbradex.ui.pokedex.components.PokedexSearchBar
import com.umbra.umbradex.ui.pokedex.components.PokemonCard
import com.umbra.umbradex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    onPokemonClick: (Int) -> Unit
) {
    val viewModel = remember {
        PokedexViewModel(
            authRepository = AuthRepository(),
            pokemonRepository = PokemonRepository(),
            favoritesRepository = FavoritesRepository(),
            livingDexRepository = LivingDexRepository()
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PurpleBackground,
                        PurpleSurface
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Pokédex",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleTertiary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurpleBackground
                )
            )

            // Content
            if (uiState.isLoading && uiState.allPokemon.isEmpty()) {
                // Loading state with shimmer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Shimmer for search bar
                    ShimmerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shimmer for grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(6) {
                            ShimmerCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.75f)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Search bar
                    PokedexSearchBar(
                        searchQuery = uiState.filters.searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter bar
                    FilterBar(
                        selectedType = uiState.filters.selectedType,
                        selectedGeneration = uiState.filters.selectedGeneration,
                        sortOrder = uiState.filters.sortOrder,
                        showFavoritesOnly = uiState.filters.showFavoritesOnly,
                        showFilters = uiState.showFilters,
                        onTypeSelect = { viewModel.selectType(it) },
                        onGenerationSelect = { viewModel.selectGeneration(it) },
                        onSortOrderChange = { viewModel.setSortOrder(it) },
                        onToggleFavorites = { viewModel.toggleFavoritesOnly() },
                        onToggleFiltersVisibility = { viewModel.toggleFiltersVisibility() },
                        onClearFilters = { viewModel.clearFilters() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result count
                    Text(
                        text = "${uiState.displayedPokemon.size} Pokémon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pokemon grid
                    if (uiState.displayedPokemon.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😢",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Pokémon Found",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try adjusting your filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = uiState.displayedPokemon,
                                key = { it.pokemon.nationalNumber }
                            ) { pokemonData ->
                                PokemonCard(
                                    pokemonData = pokemonData,
                                    onPokemonClick = onPokemonClick,
                                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}