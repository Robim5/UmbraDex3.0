package com.umbra.umbradex.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.ui.components.LoadingOverlay
import com.umbra.umbradex.ui.home.components.PetDisplay
import com.umbra.umbradex.ui.home.components.ProfileHeader
import com.umbra.umbradex.ui.home.components.TeamList
import com.umbra.umbradex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToPokemonDetail: (Int) -> Unit,
    onNavigateToTeamCreator: () -> Unit,
    onNavigateToTeamDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        HomeViewModel(
            authRepository = AuthRepository(),
            userRepository = UserRepository(),
            pokemonRepository = PokemonRepository(),
            teamRepository = TeamRepository(),
            context = context
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
                        text = "UmbraDex",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleTertiary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PurpleTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurpleBackground
                )
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.user == null) {
                    LoadingOverlay(message = "Loading your profile...")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Profile Header
                        uiState.user?.let { user ->
                            ProfileHeader(user = user)
                        }

                        // Pet Display
                        PetDisplay(
                            pokemon = uiState.equippedPokemon,
                            petMessage = uiState.petMessage,
                            showPetMessage = uiState.showPetMessage,
                            onPetClick = { viewModel.onPetClick() }
                        )
                        // Teams
                        TeamList(
                            teams = uiState.teams,
                            onTeamClick = onNavigateToTeamDetail,
                            onCreateTeamClick = onNavigateToTeamCreator
                        )

                        // Bottom spacing for navigation bar
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}