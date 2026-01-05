package com.umbra.umbradex.ui.teams

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.data.repository.PokemonRepository
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import kotlinx.coroutines.flow.collect

@Composable
fun PokemonSelectorDialog(
    onDismiss: () -> Unit,
    onPokemonSelected: (Pokemon) -> Unit,
    excludedPokemonIds: Set<Int> = emptySet()
) {
    // Estado local para a pesquisa e dados
    var searchQuery by remember { mutableStateOf("") }
    var allPokemon by remember { mutableStateOf<List<Pokemon>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pokémon filtrados baseados na pesquisa e exclusões
    val filteredPokemon = remember(searchQuery, allPokemon, excludedPokemonIds) {
        val query = searchQuery.lowercase()
        allPokemon
            .filter { pokemon ->
                if (query.isEmpty()) true
                else pokemon.name.lowercase().contains(query) ||
                        pokemon.id.toString().contains(query)
            }
            .filterNot { it.id in excludedPokemonIds }
    }

    // Carregar pokémon quando o diálogo abre
    LaunchedEffect(Unit) {
        val repository = PokemonRepository()
        repository.getAllPokemon().collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    allPokemon = resource.data
                    isLoading = false
                }
                is Resource.Error -> {
                    errorMessage = resource.message
                    isLoading = false
                }
                is Resource.Loading -> {
                    isLoading = true
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Pokémon") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Campo de pesquisa
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("Search by name or number...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                // Conteúdo baseado no estado
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    filteredPokemon.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Pokémon found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredPokemon) { pokemon ->
                                PokemonListItem(
                                    pokemon = pokemon,
                                    onClick = {
                                        onPokemonSelected(pokemon)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PokemonListItem(
    pokemon: Pokemon,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagem do Pokémon
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Informações do Pokémon
        Column {
            Text(
                text = pokemon.formattedId(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = pokemon.capitalizedName(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Tipos do Pokémon
            if (pokemon.types.isNotEmpty()) {
                Text(
                    text = pokemon.types.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}