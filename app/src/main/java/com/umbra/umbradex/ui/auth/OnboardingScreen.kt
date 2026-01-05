package com.umbra.umbradex.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.umbra.umbradex.ui.components.UmbraButton
import com.umbra.umbradex.ui.components.UmbraTextField
import com.umbra.umbradex.ui.navigation.Screen
import com.umbra.umbradex.ui.theme.UmbraBackground
import com.umbra.umbradex.ui.theme.UmbraPrimary
import com.umbra.umbradex.ui.theme.UmbraSurface
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.umbra.umbradex.R
import com.umbra.umbradex.utils.getAvatarResourceId
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umbra.umbradex.utils.Resource

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var currentStep by remember { mutableStateOf(0) }
    var birthDate by remember { mutableStateOf("") }
    var pokemonKnowledge by remember { mutableStateOf("intermediate") }
    var favoriteType by remember { mutableStateOf("fire") }
    var selectedAvatar by remember { mutableStateOf("standard_male1") }
    var selectedStarter by remember { mutableStateOf(1) }

    // Observar resultado de signup
    LaunchedEffect(authState) {
        when (authState) {
            is Resource.Success -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            }
            is Resource.Error -> {
                // Erro será mostrado no UI
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header com Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStep > 0) {
                            currentStep--
                        } else {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Step ${currentStep + 1} of 5",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (currentStep + 1) / 5f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF9C27B0),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Conteúdo dinâmico
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> BirthDateStep(
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = it }
                    )
                    1 -> PokemonKnowledgeStep(
                        selected = pokemonKnowledge,
                        onSelected = { pokemonKnowledge = it }
                    )
                    2 -> FavoriteTypeStep(
                        selected = favoriteType,
                        onSelected = { favoriteType = it }
                    )
                    3 -> AvatarSelectionStep(
                        selected = selectedAvatar,
                        onSelected = { selectedAvatar = it }
                    )
                    4 -> StarterSelectionStep(
                        selected = selectedStarter,
                        onSelected = { selectedStarter = it }
                    )
                }
            }

            // Botão Next/Finish
            Button(
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        // Última etapa - fazer signup
                        viewModel.updateOnboardingData {
                            copy(
                                birthDate = birthDate,
                                pokemonKnowledge = pokemonKnowledge,
                                favoriteType = favoriteType,
                                avatar = selectedAvatar,
                                starterId = selectedStarter
                            )
                        }
                        viewModel.signup()
                    }
                },
                enabled = !isLoading &&
                        (currentStep != 0 || birthDate.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (currentStep < 4) "Next" else "Finish",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BirthDateStep(
    birthDate: String,
    onBirthDateChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎂",
            fontSize = 64.sp
        )

        Text(
            text = "When were you born?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "We need this to ensure you're old enough to play",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            label = { Text("Birth Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9C27B0),
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = Color(0xFF9C27B0),
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun PokemonKnowledgeStep(
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎓",
            fontSize = 64.sp
        )

        Text(
            text = "How well do you know Pokémon?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        listOf(
            "beginner" to "Just starting my journey",
            "intermediate" to "I know the basics",
            "expert" to "Gotta catch 'em all!"
        ).forEach { (level, description) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(level) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected == level)
                    Color(0xFF9C27B0).copy(alpha = 0.3f)
                else
                    Color.White.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (selected == level)
                        Color(0xFF9C27B0)
                    else
                        Color.White.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = level.replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteTypeStep(
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚡",
            fontSize = 64.sp
        )

        Text(
            text = "What's your favorite type?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        val types = listOf(
            "fire", "water", "grass", "electric", "psychic",
            "dragon", "dark", "fairy", "fighting", "normal"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(types) { type ->
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelected(type) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected == type)
                        Color(0xFF9C27B0).copy(alpha = 0.3f)
                    else
                        Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (selected == type)
                            Color(0xFF9C27B0)
                        else
                            Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.replaceFirstChar { it.uppercase() },
                            fontWeight = if (selected == type) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarSelectionStep(
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "👤",
            fontSize = 64.sp
        )

        Text(
            text = "Choose your avatar",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        val avatars = listOf(
            "standard_male1", "standard_male2", "standard_male3",
            "standard_female1", "standard_female2", "standard_female3"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(avatars) { avatar ->
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSelected(avatar) },
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        3.dp,
                        if (selected == avatar)
                            Color(0xFF9C27B0)
                        else
                            Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Image(
                        painter = painterResource(id = getAvatarResourceId(avatar)),
                        contentDescription = avatar,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun StarterSelectionStep(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⭐",
            fontSize = 64.sp
        )

        Text(
            text = "Choose your starter!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        val starters = listOf(
            1 to "Bulbasaur",
            4 to "Charmander",
            7 to "Squirtle"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            starters.forEach { (id, name) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelected(id) }
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            4.dp,
                            if (selected == id)
                                Color(0xFF9C27B0)
                            else
                                Color.White.copy(alpha = 0.3f)
                        ),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                            contentDescription = name,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = name,
                        fontWeight = if (selected == id) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}