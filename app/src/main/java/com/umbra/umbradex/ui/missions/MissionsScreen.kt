package com.umbra.umbradex.ui.missions

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.umbra.umbradex.ui.missions.components.MissionCard
import com.umbra.umbradex.ui.missions.components.MissionStatsCard
import com.umbra.umbradex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen() {
    val viewModel = remember {
        MissionsViewModel(
            authRepository = AuthRepository(),
            missionRepository = MissionRepository(),
            userRepository = UserRepository()
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
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
                        text = "Missions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleTertiary
                    )
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { viewModel.toggleFilters() }) {
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
            if (uiState.isLoading && uiState.missions.isEmpty()) {
                LoadingOverlay(message = "Loading missions...")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Statistics Card
                    uiState.stats?.let { stats ->
                        MissionStatsCard(stats = stats)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // View mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.showAllMissions)
                                "${uiState.displayedMissions.size} Missions"
                            else
                                "Next 5 Missions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        TextButton(onClick = { viewModel.toggleViewMode() }) {
                            Text(
                                text = if (uiState.showAllMissions) "Show Less" else "See All",
                                color = PurpleTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (uiState.showAllMissions)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PurpleTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filters (when expanded)
                    AnimatedVisibility(
                        visible = uiState.showFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            // Rarity filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MissionRarityFilter.entries.forEach { filter ->
                                    FilterChip(
                                        selected = uiState.selectedRarity == filter,
                                        onClick = { viewModel.selectRarity(filter) },
                                        label = {
                                            Text(
                                                text = filter.displayName,
                                                fontWeight = if (uiState.selectedRarity == filter)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = getRarityColor(filter.rarity ?: "common"),
                                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.showCompletedOnly,
                                    onClick = { viewModel.toggleCompletedFilter() },
                                    label = {
                                        Text(
                                            text = "Completed Only",
                                            fontWeight = if (uiState.showCompletedOnly)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SuccessColor,
                                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                    )
                                )

                                FilterChip(
                                    selected = uiState.showIncompleteOnly,
                                    onClick = { viewModel.toggleIncompleteFilter() },
                                    label = {
                                        Text(
                                            text = "Incomplete Only",
                                            fontWeight = if (uiState.showIncompleteOnly)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WarningColor,
                                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Missions list
                    if (uiState.displayedMissions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎯",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Missions Found",
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
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = uiState.displayedMissions,
                                key = { it.mission.id }
                            ) { missionWithProgress ->
                                MissionCard(
                                    missionWithProgress = missionWithProgress,
                                    onClaimReward = {
                                        viewModel.claimReward(missionWithProgress.mission.id)
                                    }
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

enum class MissionRarityFilter(
    val displayName: String,
    val rarity: String?
) {
    ALL("All", null),
    COMMON("Common", "common"),
    RARE("Rare", "rare"),
    EPIC("Epic", "epic"),
    LEGENDARY("Legendary", "legendary")
}