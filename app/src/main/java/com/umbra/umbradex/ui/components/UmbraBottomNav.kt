package com.umbra.umbradex.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.umbra.umbradex.ui.navigation.Screen
import com.umbra.umbradex.ui.theme.UmbraAccent
import com.umbra.umbradex.ui.theme.UmbraBackground
import com.umbra.umbradex.ui.theme.UmbraPrimary
import com.umbra.umbradex.ui.theme.UmbraSurface

// Define os itens do menu
data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun UmbraBottomNav(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Pokédex", Screen.Pokedex.route, Icons.Default.List),
        BottomNavItem("Living Dex", Screen.PokeLive.route, Icons.Default.Star), // Tracker visual
        BottomNavItem("Shop", Screen.Shop.route, Icons.Default.ShoppingCart)
    )

    // Deteta em que ecrã estamos para pintar o ícone
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Só mostramos a barra se NÃO estivermos no Login/Onboarding
    val showBottomBar = currentRoute !in listOf(Screen.Login.route, Screen.SignUp.route, Screen.Onboarding.route)

    if (showBottomBar) {
        NavigationBar(
            containerColor = UmbraSurface, // Fundo escuro
            contentColor = UmbraPrimary
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Evita empilhar páginas infinitamente ao clicar
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name
                        )
                    },
                    label = {
                        if (isSelected) Text(item.name)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = UmbraAccent,
                        indicatorColor = UmbraPrimary, // A "bolinha" roxa atrás do ícone ativo
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}