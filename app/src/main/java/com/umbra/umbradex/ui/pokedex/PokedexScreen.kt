package com.umbra.umbradex.ui.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.umbra.umbradex.ui.components.PokemonCard
import com.umbra.umbradex.ui.components.UmbraBottomNav
import com.umbra.umbradex.ui.components.UmbraTextField
import com.umbra.umbradex.ui.theme.UmbraBackground
import com.umbra.umbradex.ui.theme.UmbraPrimary
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import com.umbra.umbradex.ui.navigation.Screen
import com.umbra.umbradex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokedexScreen(
    navController: NavController,
    viewModel: PokedexViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedGeneration by viewModel.selectedGeneration.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
    val showOnlyCaught by viewModel.showOnlyCaught.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { UmbraBottomNav(navController = navController) },
        containerColor = UmbraBackground,
        topBar = {
            PokedexTopBar(
                searchText = searchText,
                onSearchChange = viewModel::onSearchTextChange,
                hasActiveFilters = selectedType != null || selectedGeneration != null ||
                        showOnlyFavorites || showOnlyCaught || sortOrder != SortOrder.NUMBER,
                onFilterClick = { showFilters = !showFilters }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Painel de filtros expansível
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FilterPanel(
                    selectedType = selectedType,
                    onTypeChange = viewModel::setTypeFilter,
                    selectedGeneration = selectedGeneration,
                    onGenerationChange = viewModel::setGenerationFilter,
                    sortOrder = sortOrder,
                    onSortOrderChange = viewModel::setSortOrder,
                    showOnlyFavorites = showOnlyFavorites,
                    onToggleFavorites = viewModel::toggleFavoritesOnly,
                    showOnlyCaught = showOnlyCaught,
                    onToggleCaught = viewModel::toggleCaughtOnly,
                    onClearAll = viewModel::clearAllFilters
                )
            }

            // Conteúdo
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is PokedexUiState.Loading -> {
                        LoadingGrid()
                    }

                    is PokedexUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadPokedex() }
                        )
                    }

                    is PokedexUiState.Success -> {
                        if (state.pokemonList.isEmpty()) {
                            EmptyState(
                                onClearFilters = viewModel::clearAllFilters
                            )
                        } else {
                            PokemonGrid(
                                pokemonList = state.pokemonList,
                                filteredCount = state.filteredCount,
                                totalCount = state.totalCount,
                                onPokemonClick = { pokemon ->
                                    navController.navigate(Screen.PokemonDetail.createRoute(pokemon.id))
                                },
                                onFavoriteClick = { pokemonId ->
                                    viewModel.toggleFavorite(pokemonId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokedexTopBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    hasActiveFilters: Boolean,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UmbraSurface)
            .padding(16.dp)
    ) {
        // Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pokédex",
                color = UmbraPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            // Botão de filtros com badge
            Box {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Filtros",
                        tint = if (hasActiveFilters) UmbraAccent else Color.White
                    )
                }

                if (hasActiveFilters) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(UmbraAccent, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barra de pesquisa
        UmbraTextField(
            value = searchText,
            onValueChange = onSearchChange,
            label = "Pesquisar Pokémon...",
            icon = Icons.Default.Search,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FilterPanel(
    selectedType: String?,
    onTypeChange: (String?) -> Unit,
    selectedGeneration: Int?,
    onGenerationChange: (Int?) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    showOnlyCaught: Boolean,
    onToggleCaught: () -> Unit,
    onClearAll: () -> Unit
) {
    val types = listOf(
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice",
        "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug",
        "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    )

    val generations = (1..9).toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UmbraSurfaceHighlight)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Filtros",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            TextButton(onClick = onClearAll) {
                Text("Limpar Tudo", color = UmbraAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chips de Tipo
        Text("Tipo", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = {
                        onTypeChange(if (selectedType == type) null else type)
                    },
                    label = { Text(type, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UmbraPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chips de Geração
        Text("Geração", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            generations.forEach { gen ->
                FilterChip(
                    selected = selectedGeneration == gen,
                    onClick = {
                        onGenerationChange(if (selectedGeneration == gen) null else gen)
                    },
                    label = { Text("Gen $gen", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UmbraAccent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ordenação
        Text("Ordenar por", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOrder.values().forEach { order ->
                val label = when (order) {
                    SortOrder.NUMBER -> "Número"
                    SortOrder.NAME_ASC -> "A-Z"
                    SortOrder.NAME_DESC -> "Z-A"
                    SortOrder.TYPE -> "Tipo"
                }

                FilterChip(
                    selected = sortOrder == order,
                    onClick = { onSortOrderChange(order) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UmbraGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterChip(
                selected = showOnlyFavorites,
                onClick = onToggleFavorites,
                label = { Text("Favoritos", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE91E63),
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = showOnlyCaught,
                onClick = onToggleCaught,
                label = { Text("Capturados", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun PokemonGrid(
    pokemonList: List<com.umbra.umbradex.data.model.Pokemon>,
    filteredCount: Int,
    totalCount: Int,
    onPokemonClick: (com.umbra.umbradex.data.model.Pokemon) -> Unit,
    onFavoriteClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Contador
        Surface(
            color = UmbraSurfaceHighlight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Mostrando $filteredCount de $totalCount Pokémon",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = pokemonList,
                key = { it.id }
            ) { pokemon ->
                PokemonCard(
                    pokemon = pokemon,
                    onClick = { onPokemonClick(pokemon) }
                )
            }
        }
    }
}

@Composable
fun LoadingGrid() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = UmbraPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Carregando Pokédex...", color = Color.Gray)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = UmbraError,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = UmbraPrimary)
            ) {
                Text("Tentar Novamente")
            }
        }
    }
}

@Composable
fun EmptyState(onClearFilters: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Nenhum Pokémon encontrado",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tenta ajustar os filtros",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClearFilters,
                colors = ButtonDefaults.buttonColors(containerColor = UmbraAccent)
            ) {
                Text("Limpar Filtros")
            }
        }
    }
}

// FlowRow personalizado (se não tiver no Compose)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }

        var xPos = 0
        var yPos = 0
        var maxHeight = 0

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                if (xPos + placeable.width > constraints.maxWidth) {
                    xPos = 0
                    yPos += maxHeight + 8.dp.roundToPx()
                    maxHeight = 0
                }

                placeable.place(xPos, yPos)
                xPos += placeable.width + 8.dp.roundToPx()
                maxHeight = maxOf(maxHeight, placeable.height)
            }
        }
    }
}