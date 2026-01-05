package com.umbra.umbradex.ui.teams

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.umbra.umbradex.data.model.Team
import com.umbra.umbradex.ui.components.UmbraBottomNav
import com.umbra.umbradex.ui.theme.UmbraBackground
import com.umbra.umbradex.ui.theme.UmbraPrimary
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.utils.ImageUtils




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(
    navController: NavController,
    viewModel: TeamsViewModel = viewModel()
) {
    val context = LocalContext.current

    // Estados do ViewModel
    val teams by viewModel.teams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Estados locais para diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTeamForOptions by remember { mutableStateOf<Team?>(null) }
    var selectedTeamForEdit by remember { mutableStateOf<Team?>(null) }
    var selectedTeamForDelete by remember { mutableStateOf<Team?>(null) }
    var showPokemonSelector by remember { mutableStateOf(false) }
    var selectedTeamForPokemon by remember { mutableStateOf<Team?>(null) }
    var selectedSlotIndex by remember { mutableStateOf<Int?>(null) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var selectedPokemonForLevel by remember { mutableStateOf<Pokemon?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Teams") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (teams.size < 22) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Team")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
                teams.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No teams yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first team!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(teams) { team ->
                            TeamCard(
                                team = team,
                                onClick = {},
                                onLongClick = {
                                    selectedTeamForOptions = team
                                },
                                onPokemonSlotClick = { slotIndex ->
                                    selectedTeamForPokemon = team
                                    selectedSlotIndex = slotIndex
                                    showPokemonSelector = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de criação de equipa
    if (showCreateDialog) {
        CreateTeamDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, region ->
                viewModel.createTeam(name, region)
                showCreateDialog = false
            }
        )
    }

    // Menu de opções da equipa
    if (selectedTeamForOptions != null) {
        TeamOptionsMenu(
            teamName = selectedTeamForOptions!!.name,
            onDismiss = { selectedTeamForOptions = null },
            onEdit = {
                selectedTeamForEdit = selectedTeamForOptions
                selectedTeamForOptions = null
            },
            onDownload = {
                viewModel.downloadTeamCard(context, selectedTeamForOptions!!)
                selectedTeamForOptions = null
            },
            onDelete = {
                selectedTeamForDelete = selectedTeamForOptions
                selectedTeamForOptions = null
            }
        )
    }

    // Diálogo de edição do nome
    if (selectedTeamForEdit != null) {
        EditTeamNameDialog(
            currentName = selectedTeamForEdit!!.name,
            onDismiss = { selectedTeamForEdit = null },
            onConfirm = { newName ->
                viewModel.updateTeamName(selectedTeamForEdit!!.id, newName)
                selectedTeamForEdit = null
            }
        )
    }

    // Diálogo de confirmação de eliminação
    if (selectedTeamForDelete != null) {
        DeleteConfirmationDialog(
            teamName = selectedTeamForDelete!!.name,
            onDismiss = { selectedTeamForDelete = null },
            onConfirm = {
                viewModel.deleteTeam(selectedTeamForDelete!!.id)
                selectedTeamForDelete = null
            }
        )
    }

    // Seletor de Pokémon
    if (showPokemonSelector && selectedTeamForPokemon != null && selectedSlotIndex != null) {
        PokemonSelectorDialog(
            onDismiss = { showPokemonSelector = false },
            onPokemonSelected = { pokemon ->
                selectedPokemonForLevel = pokemon
                showPokemonSelector = false
                showLevelDialog = true
            },
            excludedPokemonIds = emptySet()
        )
    }

    // Diálogo de nível do Pokémon
    if (showLevelDialog && selectedPokemonForLevel != null && selectedTeamForPokemon != null && selectedSlotIndex != null) {
        PokemonLevelDialog(
            pokemon = selectedPokemonForLevel!!,
            onDismiss = {
                showLevelDialog = false
                selectedPokemonForLevel = null
            },
            onConfirm = { level ->
                selectedTeamForPokemon?.let { team ->
                    selectedSlotIndex?.let { slot ->
                        viewModel.addOrReplacePokemonInTeam(
                            teamId = team.id,
                            slotIndex = slot,
                            pokemon = selectedPokemonForLevel!!,
                            level = level
                        )
                    }
                }
                showLevelDialog = false
                selectedPokemonForLevel = null
                selectedTeamForPokemon = null
                selectedSlotIndex = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamCard(
    team: Team,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPokemonSlotClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = team.gradientColors.map { Color(android.graphics.Color.parseColor(it)) }
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Cabeçalho da equipa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = team.region,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text = "${team.pokemon.size}/6",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid de Pokémon (2 colunas x 3 linhas)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..1) {
                                val slotIndex = row * 2 + col
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PokemonSlot(
                                        pokemon = team.pokemon.find { it.slotIndex == slotIndex },
                                        onClick = { onPokemonSlotClick(slotIndex) }
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
fun PokemonSlot(
    pokemon: Team.TeamPokemon?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pokemon != null) Color.White.copy(alpha = 0.9f)
            else Color.White.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (pokemon != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = pokemon.imageUrl,
                        contentDescription = pokemon.name,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pokemon.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Lv.${pokemon.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Pokémon",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, region: String) -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("Kanto") }
    var expanded by remember { mutableStateOf(false) }

    val regions = listOf(
        "Kanto", "Johto", "Hoenn", "Sinnoh", "Unova",
        "Kalos", "Alola", "Galar", "Paldea"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Team") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRegion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Region") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        regions.forEach { region ->
                            DropdownMenuItem(
                                text = { Text(region) },
                                onClick = {
                                    selectedRegion = region
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (teamName.isNotBlank()) {
                        onConfirm(teamName, selectedRegion)
                    }
                },
                enabled = teamName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TeamOptionsMenu(
    teamName: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Team Options") },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("Edit Name") },
                    leadingContent = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        onEdit()
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Download Card") },
                    leadingContent = {
                        Icon(Icons.Default.Download, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        onDownload()
                        onDismiss()
                    }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Delete Team") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    },
                    modifier = Modifier.clickable {
                        onDelete()
                    }
                )
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
fun EditTeamNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Team Name") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Team Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName)
                    }
                },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    teamName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Delete Team?") },
        text = {
            Text("Are you sure you want to delete \"$teamName\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}