package com.umbra.umbradex.ui.pokedex.components

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.umbra.umbradex.ui.pokedex.SortOrder
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    selectedType: String?,
    selectedGeneration: Int?,
    sortOrder: SortOrder,
    showFavoritesOnly: Boolean,
    showFilters: Boolean,
    onTypeSelect: (String?) -> Unit,
    onGenerationSelect: (Int?) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onToggleFavorites: () -> Unit,
    onToggleFiltersVisibility: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter toggle button
                FilterChip(
                    selected = showFilters,
                    onClick = onToggleFiltersVisibility,
                    label = {
                        Text(
                            text = if (showFilters) "Hide Filters" else "Show Filters",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = TextPrimary,
                        selectedLeadingIconColor = TextPrimary
                    )
                )

                // Favorites only
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = onToggleFavorites,
                    label = {
                        Text(
                            text = "Favorites",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showFavoritesOnly)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErrorColor,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }

            // Clear filters
            if (selectedType != null || selectedGeneration != null || sortOrder != SortOrder.NUMBER_ASC) {
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Clear",
                        color = PurpleTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Expanded filters
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sort order
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOrder.entries.forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = { onSortOrderChange(order) },
                            label = {
                                Text(
                                    text = when (order) {
                                        SortOrder.NUMBER_ASC -> "# ↑"
                                        SortOrder.NUMBER_DESC -> "# ↓"
                                        SortOrder.NAME_AZ -> "A-Z"
                                        SortOrder.NAME_ZA -> "Z-A"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = TextPrimary
                            )
                        )
                    }
                }

                Divider(color = PurpleSurfaceVariant)

                // Generation filter
                Text(
                    text = "Generation",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGeneration == null,
                        onClick = { onGenerationSelect(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = TextPrimary
                        )
                    )

                    (1..9).forEach { gen ->
                        FilterChip(
                            selected = selectedGeneration == gen,
                            onClick = { onGenerationSelect(gen) },
                            label = { Text("Gen $gen") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = TextPrimary
                            )
                        )
                    }
                }

                Divider(color = PurpleSurfaceVariant)

                // Type filter
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { onTypeSelect(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = TextPrimary
                        )
                    )

                    Constants.POKEMON_TYPES.forEach { type ->
                        val typeColor = getTypeColor(type)
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeSelect(type) },
                            label = {
                                Text(
                                    text = type.replaceFirstChar { it.uppercase() },
                                    color = if (selectedType == type) Color.White else TextPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = typeColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}