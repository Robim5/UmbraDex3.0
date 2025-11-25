package com.umbra.umbradex.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Purple Theme
val PurplePrimary = Color(0xFF9C27B0)
val PurpleSecondary = Color(0xFF7B1FA2)
val PurpleTertiary = Color(0xFFBA68C8)
val PurpleLight = Color(0xFFE1BEE7)

// Dark Theme Colors
val PurpleBackground = Color(0xFF1A0B1F)
val PurpleSurface = Color(0xFF2D1B3D)
val PurpleSurfaceVariant = Color(0xFF3D2A4D)

// Light Theme Colors (if you want to add later)
val PurpleBackgroundLight = Color(0xFFF3E5F5)
val PurpleSurfaceLight = Color(0xFFFFFFFF)

// Rarity Colors
val CommonColor = Color(0xFF9E9E9E)
val RareColor = Color(0xFF2196F3)
val EpicColor = Color(0xFF9C27B0)
val LegendaryColor = Color(0xFFFFD700)

// Type Colors (Pokemon Types)
val TypeNormal = Color(0xFFA8A878)
val TypeFire = Color(0xFFF08030)
val TypeWater = Color(0xFF6890F0)
val TypeElectric = Color(0xFFF8D030)
val TypeGrass = Color(0xFF78C850)
val TypeIce = Color(0xFF98D8D8)
val TypeFighting = Color(0xFFC03028)
val TypePoison = Color(0xFFA040A0)
val TypeGround = Color(0xFFE0C068)
val TypeFlying = Color(0xFFA890F0)
val TypePsychic = Color(0xFFF85888)
val TypeBug = Color(0xFFA8B820)
val TypeRock = Color(0xFFB8A038)
val TypeGhost = Color(0xFF705898)
val TypeDragon = Color(0xFF7038F8)
val TypeDark = Color(0xFF705848)
val TypeSteel = Color(0xFFB8B8D0)
val TypeFairy = Color(0xFFEE99AC)

// UI Colors
val ErrorColor = Color(0xFFCF6679)
val SuccessColor = Color(0xFF4CAF50)
val WarningColor = Color(0xFFFF9800)
val InfoColor = Color(0xFF2196F3)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB3FFFFFF)
val TextDisabled = Color(0x61FFFFFF)

// Get Type Color Helper
fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "normal" -> TypeNormal
        "fire" -> TypeFire
        "water" -> TypeWater
        "electric" -> TypeElectric
        "grass" -> TypeGrass
        "ice" -> TypeIce
        "fighting" -> TypeFighting
        "poison" -> TypePoison
        "ground" -> TypeGround
        "flying" -> TypeFlying
        "psychic" -> TypePsychic
        "bug" -> TypeBug
        "rock" -> TypeRock
        "ghost" -> TypeGhost
        "dragon" -> TypeDragon
        "dark" -> TypeDark
        "steel" -> TypeSteel
        "fairy" -> TypeFairy
        else -> CommonColor
    }
}

// Get Rarity Color Helper
fun getRarityColor(rarity: String): Color {
    return when (rarity.lowercase()) {
        "common" -> CommonColor
        "rare" -> RareColor
        "epic" -> EpicColor
        "legendary" -> LegendaryColor
        else -> CommonColor
    }
}