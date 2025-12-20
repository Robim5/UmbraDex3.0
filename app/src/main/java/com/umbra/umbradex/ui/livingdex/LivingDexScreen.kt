package com.umbra.umbradex.ui.livingdex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import com.umbra.umbradex.ui.livingdex.components.LivingDexPokemonCard
//import com.umbra.umbradex.ui.livingdex.components.StatsCard
import com.umbra.umbradex.ui.pokedex.components.FilterBar
import com.umbra.umbradex.ui.pokedex.components.PokedexSearchBar
import com.umbra.umbradex.ui.theme.*
import androidx.compose.runtime.Composable
import com.umbra.umbradex.ui.livingdex.components.StatsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivingDexScreen(
    onPokemonClick: (Int) -> Unit
) {
    val viewModel = remember {
        LivingDexViewModel(
            authRepository = AuthRepository(),
            pokemonRepository = PokemonRepository(),
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
                        text = "Living Dex",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleTertiary
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFiltersVisibility() }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = PurpleTertiary
                        )
                    }
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
                    // Shimmer for stats
                    ShimmerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shimmer for grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(9) {
                            ShimmerCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
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
                    // Statistics Card
                    uiState.stats?.let { stats ->
                        StatsCard(
                            stats = stats,
                            topTypes = uiState.topTypes
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search bar
                    PokedexSearchBar(
                        searchQuery = uiState.filters.searchQuery,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter bar (collapsed)
                    if (uiState.showFilters) {
                        FilterBar(
                            selectedType = uiState.filters.selectedType,
                            selectedGeneration = uiState.filters.selectedGeneration,
                            sortOrder = uiState.filters.sortOrder,
                            showFavoritesOnly = false,
                            showFilters = true,
                            onTypeSelect = { viewModel.selectType(it) },
                            onGenerationSelect = { viewModel.selectGeneration(it) },
                            onSortOrderChange = { viewModel.setSortOrder(it) },
                            onToggleFavorites = { },
                            onToggleFiltersVisibility = { viewModel.toggleFiltersVisibility() },
                            onClearFilters = { viewModel.clearFilters() }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Show owned filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.displayedPokemon.size} Pokémon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        FilterChip(
                            selected = uiState.filters.showOwnedOnly,
                            onClick = { viewModel.toggleOwnedOnly() },
                            label = {
                                Text(
                                    text = if (uiState.filters.showOwnedOnly) "Owned Only" else "Show All",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SuccessColor,
                                selectedLabelColor = TextPrimary
                            )
                        )
                    }

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
                                    text = "📋",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (uiState.filters.showOwnedOnly) "No Pokémon Owned Yet" else "No Pokémon Found",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (uiState.filters.showOwnedOnly) "Start catching Pokémon!" else "Try adjusting your filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = uiState.displayedPokemon,
                                key = { it.pokemon.nationalNumber }
                            ) { pokemonData ->
                                LivingDexPokemonCard(
                                    pokemonData = pokemonData,
                                    onPokemonClick = onPokemonClick,
                                    onToggleOwned = { viewModel.toggleOwned(it) }
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