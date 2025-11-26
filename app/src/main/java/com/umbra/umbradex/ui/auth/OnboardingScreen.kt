package com.umbra.umbradex.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umbra.umbradex.R
import com.umbra.umbradex.data.model.OnboardingData
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.Constants
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OnboardingScreen(
    authState: AuthState,
    onComplete: (OnboardingData) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }

    // Onboarding data
    var birthDate by remember { mutableStateOf("") }
    var pokemonKnowledge by remember { mutableStateOf("") }
    var favoriteType by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var starterId by remember { mutableIntStateOf(0) }

    val isLoading = authState is AuthState.Loading

    Box(
        modifier = modifier
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Progress indicator
            OnboardingProgressBar(
                currentStep = currentStep,
                totalSteps = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Content based on current step
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> BirthDateStep(
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = it }
                    )
                    1 -> PokemonKnowledgeStep(
                        selectedKnowledge = pokemonKnowledge,
                        onKnowledgeSelect = { pokemonKnowledge = it }
                    )
                    2 -> FavoriteTypeStep(
                        selectedType = favoriteType,
                        onTypeSelect = { favoriteType = it }
                    )
                    3 -> AvatarSelectionStep(
                        selectedAvatar = selectedAvatar,
                        username = username,
                        onAvatarSelect = { selectedAvatar = it },
                        onUsernameChange = { username = it }
                    )
                    4 -> StarterSelectionStep(
                        selectedStarter = starterId,
                        onStarterSelect = { starterId = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            if (authState is AuthState.Error) {
                Text(
                    text = authState.message,
                    color = ErrorColor,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PurpleTertiary
                        ),
                        border = BorderStroke(1.dp, PurpleTertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back")
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }

                // Next/Complete button
                Button(
                    onClick = {
                        if (currentStep < 4) {
                            currentStep++
                        } else {
                            // Complete onboarding
                            val onboardingData = OnboardingData(
                                birthDate = birthDate,
                                pokemonKnowledge = pokemonKnowledge,
                                favoriteType = favoriteType,
                                avatarUrl = selectedAvatar,
                                username = username,
                                starterId = starterId
                            )
                            onComplete(onboardingData)
                        }
                    },
                    enabled = !isLoading && isStepValid(
                        step = currentStep,
                        birthDate = birthDate,
                        pokemonKnowledge = pokemonKnowledge,
                        favoriteType = favoriteType,
                        selectedAvatar = selectedAvatar,
                        username = username,
                        starterId = starterId
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurplePrimary,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (currentStep < 4) "Next" else "Complete")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (currentStep < 4)
                                Icons.Default.ArrowForward
                            else
                                Icons.Default.Check,
                            contentDescription = if (currentStep < 4) "Next" else "Complete"
                        )
                    }
                }
            }
        }
    }
}

// Progress Bar
@Composable
fun OnboardingProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Step ${currentStep + 1} of $totalSteps",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LinearProgressIndicator(
            progress = (currentStep + 1).toFloat() / totalSteps,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PurplePrimary,
            trackColor = PurpleSurfaceVariant,
        )
    }
}

// Step 1: Birth Date
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDateStep(
    birthDate: String,
    onBirthDateChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎂",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "When's Your Birthday?",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We need to know you're old enough to catch 'em all!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState()

        OutlinedTextField(
            value = birthDate,
            onValueChange = { },
            label = { Text("Birth Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                        contentDescription = "Select date",
                        tint = PurpleTertiary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = PurpleTertiary,
                focusedLabelColor = PurplePrimary,
            )
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                onBirthDateChange(formatter.format(date))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK", color = PurplePrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = PurpleSurface,
                        titleContentColor = TextPrimary,
                        headlineContentColor = TextPrimary,
                        weekdayContentColor = TextSecondary,
                        subheadContentColor = TextPrimary,
                        yearContentColor = TextPrimary,
                        currentYearContentColor = PurplePrimary,
                        selectedYearContentColor = TextPrimary,
                        selectedYearContainerColor = PurplePrimary,
                        dayContentColor = TextPrimary,
                        selectedDayContentColor = TextPrimary,
                        selectedDayContainerColor = PurplePrimary,
                        todayContentColor = PurpleTertiary,
                        todayDateBorderColor = PurpleTertiary
                    )
                )
            }
        }
    }
}

// Step 2: Pokemon Knowledge
@Composable
fun PokemonKnowledgeStep(
    selectedKnowledge: String,
    onKnowledgeSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚡",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Your Pokémon Knowledge?",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How well do you know the Pokémon world?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        val knowledgeOptions = listOf(
            "love" to "I LOVE Pokémon! 💖",
            "some" to "I know some things 🤔",
            "lot" to "I know A LOT! 🧠"
        )

        knowledgeOptions.forEach { (value, label) ->
            KnowledgeOptionCard(
                label = label,
                isSelected = selectedKnowledge == value,
                onClick = { onKnowledgeSelect(value) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun KnowledgeOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurplePrimary else PurpleSurfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, PurpleTertiary) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// Step 3: Favorite Type
@Composable
fun FavoriteTypeStep(
    selectedType: String,
    onTypeSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔥",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Favorite Type?",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Which type speaks to your soul?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp),
            userScrollEnabled = false
        ) {
            items(Constants.POKEMON_TYPES) { type ->
                TypeCard(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelect(type) }
                )
            }
        }
    }
}

@Composable
fun TypeCard(
    type: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val typeColor = getTypeColor(type)

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) typeColor else PurpleSurfaceVariant
        ),
        border = if (isSelected) BorderStroke(3.dp, PurpleTertiary) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Step 4: Avatar & Username
@Composable
fun AvatarSelectionStep(
    selectedAvatar: String,
    username: String,
    onAvatarSelect: (String) -> Unit,
    onUsernameChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👤",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Create Your Trainer",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose your avatar and trainer name",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Username input
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Trainer Name") },
            placeholder = { Text("Ash Ketchum") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = PurpleTertiary,
                focusedLabelColor = PurplePrimary,
                cursorColor = PurplePrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Your Avatar",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Male avatars
        Text(
            text = "Male",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                val avatarName = "male${index + 1}"
                AvatarCard(
                    avatarRes = getAvatarResourceId("male_start", avatarName),
                    isSelected = selectedAvatar == avatarName,
                    onClick = { onAvatarSelect(avatarName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Female avatars
        Text(
            text = "Female",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                val avatarName = "female${index + 1}"
                AvatarCard(
                    avatarRes = getAvatarResourceId("female_start", avatarName),
                    isSelected = selectedAvatar == avatarName,
                    onClick = { onAvatarSelect(avatarName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AvatarCard(
    avatarRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (isSelected) PurplePrimary else PurpleSurfaceVariant)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) PurpleTertiary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(if (isSelected) 4.dp else 0.dp)
    ) {
        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = "Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// Helper function to get avatar resource ID
fun getAvatarResourceId(folder: String, avatarName: String): Int {
    // This is a placeholder - you'll need to map these to actual drawable resources
    return when (avatarName) {
        "male1" -> R.drawable.standard_male1
        "male2" -> R.drawable.standard_male2
        "male3" -> R.drawable.standard_male3
        "male4" -> R.drawable.standard_male4
        "male5" -> R.drawable.standard_male5
        "female1" -> R.drawable.standard_female1
        "female2" -> R.drawable.standard_female2
        "female3" -> R.drawable.standard_female3
        "female4" -> R.drawable.standard_female4
        "female5" -> R.drawable.standard_female5
        else -> R.drawable.default_person
    }
}

// Step 5: Starter Selection
@Composable
fun StarterSelectionStep(
    selectedStarter: Int,
    onStarterSelect: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // Classic starters: Bulbasaur (1), Charmander (4), Squirtle (7)
    val starters = listOf(
        1 to "Bulbasaur",
        4 to "Charmander",
        7 to "Squirtle"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎮",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Choose Your Starter!",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Which Pokémon will be your partner?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        starters.forEach { (id, name) ->
            StarterCard(
                pokemonId = id,
                pokemonName = name,
                isSelected = selectedStarter == id,
                onClick = { onStarterSelect(id) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StarterCard(
    pokemonId: Int,
    pokemonName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val spriteUrl = "${Constants.POKEAPI_OFFICIAL_ARTWORK}$pokemonId.png"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PurplePrimary else PurpleSurfaceVariant
        ),
        border = if (isSelected) BorderStroke(3.dp, PurpleTertiary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = spriteUrl,
                contentDescription = pokemonName,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = pokemonName,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "#${pokemonId.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) TextSecondary else TextDisabled
                )
            }
        }
    }
}

// Validation helper
private fun isStepValid(
    step: Int,
    birthDate: String,
    pokemonKnowledge: String,
    favoriteType: String,
    selectedAvatar: String,
    username: String,
    starterId: Int
): Boolean {
    return when (step) {
        0 -> birthDate.isNotBlank()
        1 -> pokemonKnowledge.isNotBlank()
        2 -> favoriteType.isNotBlank()
        3 -> selectedAvatar.isNotBlank() && username.isNotBlank() && username.length >= 3
        4 -> starterId > 0
        else -> false
    }
}