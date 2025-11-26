package com.umbra.umbradex.ui.navigation

import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Stream

sealed class Screen(val route: String) {
    // Main screens with bottom nav
    object Home : Screen("home")
    object Pokedex : Screen("pokedex")
    object LivingDex : Screen("living_dex")
    object Shop : Screen("shop")
    object Missions : Screen("missions")
    object Inventory : Screen("inventory")

    // Detail screens (no bottom nav)
    object PokemonDetail : Screen("pokemon_detail/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "pokemon_detail/$pokemonId"
    }
    object Settings : Screen("settings")
    object TeamCreator : Screen("team_creator")
    object TeamDetail : Screen("team_detail/{teamId}") {
        fun createRoute(teamId: String) = "team_detail/$teamId"
    }
}

// Bottom nav items
enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME(
        route = Screen.Home.route,
        title = "Home",
        icon = androidx.compose.material.icons.Icons.Default.Home
    ),
    POKEDEX(
        route = Screen.Pokedex.route,
        title = "Pokédex",
        icon = androidx.compose.material.icons.Icons.Default.Menu
    ),
    LIVING_DEX(
        route = Screen.LivingDex.route,
        title = "Living Dex",
        icon = androidx.compose.material.icons.Icons.Default.List
    ),
    SHOP(
        route = Screen.Shop.route,
        title = "Shop",
        icon = androidx.compose.material.icons.Icons.Default.AddShoppingCart
    ),
    MISSIONS(
        route = Screen.Missions.route,
        title = "Missions",
        icon = androidx.compose.material.icons.Icons.Default.Stream
    ),
    INVENTORY(
        route = Screen.Inventory.route,
        title = "Inventory",
        icon = androidx.compose.material.icons.Icons.Default.Inventory
    )
}