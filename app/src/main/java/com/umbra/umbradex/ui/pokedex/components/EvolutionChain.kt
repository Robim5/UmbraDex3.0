package com.umbra.umbradex.ui.pokedex.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umbra.umbradex.data.model.EvolutionChainItem
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.Constants

@Composable
fun EvolutionChain(
    evolutionChain: List<EvolutionChainItem>,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (evolutionChain.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PurpleSurface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No evolution data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            evolutionChain.forEachIndexed { index, evolution ->
                item {
                    EvolutionStage(
                        evolution = evolution,
                        onClick = { onPokemonClick(evolution.id) }
                    )
                }

                 // Arrow between evolutions
                if (index < evolutionChain.lastIndex) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp) // Add some spacing for the arrow
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Evolves to",
                                tint = PurpleTertiary,
                                modifier = Modifier.size(24.dp)
                            )

                            evolutionChain[index + 1].minLevel?.let { level ->
                                Text(
                                    text = "Lv.$level",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            evolutionChain[index + 1].trigger?.let { trigger ->
                                if (trigger != "level-up") {
                                    Text(
                                        text = trigger,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvolutionStage(
    evolution: EvolutionChainItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = PurpleSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "${Constants.POKEAPI_OFFICIAL_ARTWORK}${evolution.id}.png",
                contentDescription = evolution.name,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = evolution.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "#${evolution.id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}