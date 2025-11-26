package com.umbra.umbradex.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.umbra.umbradex.ui.home.HomeScreen
import com.umbra.umbradex.ui.pokedex.PokedexScreen
import com.umbra.umbradex.ui.pokedex.PokemonDetailScreen
import com.umbra.umbradex.ui.livingdex.LivingDexScreen
import com.umbra.umbradex.ui.shop.ShopScreen
import com.umbra.umbradex.ui.missions.MissionsScreen
import com.umbra.umbradex.ui.inventory.InventoryScreen
import com.umbra.umbradex.ui.settings.SettingsScreen

@Composable
fun MainNavigation(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Show bottom nav only on main screens
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val showBottomBar = BottomNavItem.entries.any { it.route == currentRoute }

            if (showBottomBar) {
                UmbraDexBottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToPokemonDetail = { pokemonId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                    },
                    onNavigateToTeamCreator = {
                        navController.navigate(Screen.TeamCreator.route)
                    },
                    onNavigateToTeamDetail = { teamId ->
                        navController.navigate(Screen.TeamDetail.createRoute(teamId))
                    }
                )
            }

            // Pokedex Screen
            composable(Screen.Pokedex.route) {
                PokedexScreen(
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                    }
                )
            }

            // Pokemon Detail Screen
            composable(
                route = Screen.PokemonDetail.route,
                arguments = listOf(
                    navArgument("pokemonId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: 1
                PokemonDetailScreen(
                    pokemonId = pokemonId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Living Dex Screen
            composable(Screen.LivingDex.route) {
                LivingDexScreen(
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                    }
                )
            }

            // Shop Screen
            composable(Screen.Shop.route) {
                ShopScreen()
            }

            // Missions Screen
            composable(Screen.Missions.route) {
                MissionsScreen()
            }

            // Inventory Screen
            composable(Screen.Inventory.route) {
                InventoryScreen(
                    onPokemonClick = { pokemonId ->
                        navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                    }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogout = onLogout
                )
            }
        }
    }
}