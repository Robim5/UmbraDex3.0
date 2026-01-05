package com.umbra.umbradex.ui.home

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.umbra.umbradex.R
import com.umbra.umbradex.ui.components.AnimatedCircularChart
import com.umbra.umbradex.ui.components.UmbraBottomNav
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.utils.getAvatarResourceId
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign
import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.ui.navigation.Screen
import com.umbra.umbradex.utils.toColor
import kotlin.math.roundToInt

@Composable
fun StartPageScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val petClickCount by viewModel.petClickCount.collectAsState()

    // MediaPlayer para som do pet
    val petSoundPlayer = remember {
        MediaPlayer.create(context, R.raw.goodanimal) // Certifica-te que tens este ficheiro
    }

    DisposableEffect(Unit) {
        onDispose {
            petSoundPlayer?.release()
        }
    }

    Scaffold(
        bottomBar = { UmbraBottomNav(navController = navController) },
        containerColor = UmbraBackground
    ) { paddingValues ->

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = UmbraPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carregando perfil...", color = Color.Gray)
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = UmbraError,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = UmbraError, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }

            is HomeUiState.Success -> {
                StartPageContent(
                    state = state,
                    navController = navController,
                    petClickCount = petClickCount,
                    onPetClick = {
                        viewModel.onPetClick()
                        try {
                            petSoundPlayer?.start()
                        } catch (e: Exception) {
                            // Som falhou, ignora
                        }
                    },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun StartPageContent(
    state: HomeUiState.Success,
    navController: NavController,
    petClickCount: Int,
    onPetClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val profile = state.profile
    val scrollState = rememberScrollState()
    var isStatsExpanded by remember { mutableStateOf(false) }

    // Animação de rotação do pet (360º após 3 cliques)
    val petRotation by animateFloatAsState(
        targetValue = if (petClickCount >= 3) 360f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "petRotation"
    )

    // Mensagens aleatórias do Pokémon
    val petMessages = remember {
        listOf(
            "Pika pika! ⚡",
            "Olá, treinador!",
            "Pronto para aventuras! 🎮",
            "Vamos capturar mais Pokémon!",
            "És o melhor treinador! 🏆"
        )
    }
    var currentPetMessage by remember { mutableStateOf("") }

    // Parse da cor do nome
    val nameColors = remember(profile.equippedNameColor) {
        try {
            profile.equippedNameColor
                .removeSurrounding("[", "]")
                .replace("\"", "")
                .split(",")
                .map { it.trim() }
        } catch (e: Exception) {
            listOf("#FFFFFF")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // --- HEADER ---
        HomeHeader(
            profile = profile,
            nameColors = nameColors,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- ÁREA DO TREINADOR (Avatar + Pet) ---
        TrainerSection(
            profile = profile,
            petRotation = petRotation,
            petClickCount = petClickCount,
            onPetClick = {
                onPetClick()
                currentPetMessage = petMessages.random()
            },
            petMessage = currentPetMessage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- BARRA DE NÍVEL ---
        LevelProgressBar(profile = profile)

        Spacer(modifier = Modifier.height(24.dp))

        // --- SEÇÃO DE ESTATÍSTICAS (PERGAMINHO) ---
        StatsSection(
            state = state,
            isExpanded = isStatsExpanded,
            onToggleExpand = { isStatsExpanded = !isStatsExpanded },
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(80.dp)) // Espaço para bottom nav
    }
}

@Composable
fun HomeHeader(
    profile: UserProfile,
    nameColors: List<String>,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UmbraSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nome da App com cor personalizada
        Text(
            text = "UmbraDex",
            style = MaterialTheme.typography.headlineSmall.copy(
                brush = Brush.linearGradient(nameColors.map { it.toColor() })
            ),
            fontWeight = FontWeight.ExtraBold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nível
            Surface(
                color = UmbraPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = UmbraPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Lv ${profile.level}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Gold
            Surface(
                color = UmbraGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.money),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${profile.gold}",
                        color = UmbraGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Settings
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Definições",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun TrainerSection(
    profile: UserProfile,
    petRotation: Float,
    petClickCount: Int,
    onPetClick: () -> Unit,
    petMessage: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        // Fundo decorativo
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            UmbraPrimary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do Jogador
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 20.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = getAvatarResourceId(profile.equippedSkin)),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(3.dp, UmbraPrimary, CircleShape)
                    )

                    // Badge
                    if (profile.equippedBadge.isNotBlank()) {
                        Image(
                            painter = painterResource(id = getAvatarResourceId(profile.equippedBadge)),
                            contentDescription = "Badge",
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 8.dp, y = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Título
                Surface(
                    color = UmbraAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = profile.equippedTitle,
                        color = UmbraAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Pokémon Pet
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-10).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clickable(onClick = onPetClick),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${profile.equippedPokemonId ?: 1}.png",
                        contentDescription = "Pet",
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(petRotation)
                            .scale(if (petClickCount > 0 && petClickCount < 3) 1.1f else 1f)
                    )
                }

                // Mensagem do Pokémon
                if (petMessage.isNotBlank()) {
                    Surface(
                        color = UmbraSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = petMessage,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelProgressBar(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Nível ${profile.level}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "${profile.xp} / ${profile.xpForNextLevel} XP",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val progress = (profile.xp.toFloat() / profile.xpForNextLevel.toFloat()).coerceIn(0f, 1f)

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = UmbraPrimary,
            trackColor = UmbraSurfaceHighlight
        )
    }
}

@Composable
fun StatsSection(
    state: HomeUiState.Success,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    viewModel: HomeViewModel
) {
    val completionPercentage = state.pokedexCaught.toFloat() / state.pokedexTotal.toFloat()
    val rankTitle = viewModel.getRankTitle(completionPercentage)
    val rankColor = viewModel.getRankColor(completionPercentage)

    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "iconRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UmbraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Header (sempre visível)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estatísticas de Coleção",
                        style = MaterialTheme.typography.titleMedium,
                        color = UmbraPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rank: $rankTitle",
                        color = rankColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.rotate(iconRotation)
                )
            }

            // Conteúdo expansível
            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Divider(color = UmbraSurfaceHighlight, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Gráficos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnimatedCircularChart(
                            value = state.pokedexCaught,
                            max = state.pokedexTotal,
                            label = "Pokédex",
                            color = UmbraPrimary,
                            size = 100.dp
                        )

                        AnimatedCircularChart(
                            value = state.missionsCompleted,
                            max = state.missionsTotal,
                            label = "Missões",
                            color = UmbraAccent,
                            size = 100.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats extras
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Tempo Online",
                            value = formatTime(state.totalTimeSeconds)
                        )
                        StatItem(
                            label = "Gold Total",
                            value = "${state.profile.totalGoldEarned}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    return if (hours > 0) "${hours}h" else "${seconds / 60}m"
}