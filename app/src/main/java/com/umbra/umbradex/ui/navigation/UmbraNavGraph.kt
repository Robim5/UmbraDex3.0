package com.umbra.umbradex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.umbra.umbradex.ui.auth.AuthViewModel
import com.umbra.umbradex.ui.auth.LoginScreen
import com.umbra.umbradex.ui.auth.OnboardingScreen
import com.umbra.umbradex.ui.auth.SignUpScreen
import com.umbra.umbradex.ui.home.StartPageScreen
import com.umbra.umbradex.ui.pokedex.PokemonDetailScreen
import com.umbra.umbradex.ui.navigation.Screen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.umbra.umbradex.ui.pokedex.PokedexScreen
import com.umbra.umbradex.ui.pokelive.PokeLiveScreen
import com.umbra.umbradex.ui.shop.ShopScreen
import com.umbra.umbradex.ui.missions.MissionsScreen
import com.umbra.umbradex.ui.inventory.InventoryScreen
import com.umbra.umbradex.ui.teams.TeamsScreen
import com.umbra.umbradex.ui.settings.SettingsScreen

@Composable
fun UmbraNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()

    // Determinar rota inicial
    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ==================== AUTH SCREENS ====================

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // ==================== MAIN APP SCREENS ====================

        composable(Screen.Home.route) {
            if (currentUserId != null) {
                StartPageScreen(navController = navController)
            }
        }

        composable(Screen.Pokedex.route) {
            if (currentUserId != null) {
                PokedexScreen(navController = navController)
            }
        }

        composable(
            route = Screen.PokemonDetail.route,
            arguments = listOf(navArgument("pokemonId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pokemonId = backStackEntry.arguments?.getInt("pokemonId") ?: 1
            if (currentUserId != null) {
                PokemonDetailScreen(
                    navController = navController,
                    pokemonId = pokemonId
                )
            }
        }

        composable(Screen.PokeLive.route) {
            if (currentUserId != null) {
                PokeLiveScreen(navController = navController)
            }
        }

        composable(Screen.Shop.route) {
            if (currentUserId != null) {
                ShopScreen(userId = currentUserId!!)
            }
        }

        composable(Screen.Missions.route) {
            if (currentUserId != null) {
                MissionsScreen(userId = currentUserId!!)
            }
        }

        composable(Screen.Inventory.route) {
            if (currentUserId != null) {
                InventoryScreen(userId = currentUserId!!)
            }
        }

        composable(Screen.Teams.route) {
            if (currentUserId != null) {
                TeamsScreen(navController = navController)
            }
        }

        composable(Screen.Settings.route) {
            if (currentUserId != null) {
                SettingsScreen(
                    navController = navController,
                    userId = currentUserId!!,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    // Observar autenticação
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated && navController.currentDestination?.route !in listOf(
                Screen.Login.route,
                Screen.SignUp.route,
                Screen.Onboarding.route
            )) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}