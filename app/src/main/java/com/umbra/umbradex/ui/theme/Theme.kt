package com.umbra.umbradex.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Função auxiliar para criar cores a partir de HEX seguro
fun fromHex(hex: String, default: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}

@Composable
fun UmbraDexTheme(
    // Recebe a paleta de cores do perfil (se existir)
    themeColors: List<String>? = null,
    content: @Composable () -> Unit
) {
    // 1. Definir as cores base (Roxo Default ou Tema Personalizado)
    val primaryColor = if (!themeColors.isNullOrEmpty()) fromHex(themeColors[0], UmbraPrimary) else UmbraPrimary
    val secondaryColor = if (themeColors != null && themeColors.size > 1) fromHex(themeColors[1], UmbraAccent) else UmbraAccent
    val backgroundColor = if (themeColors != null && themeColors.size > 3) fromHex(themeColors[3], UmbraBackground) else UmbraBackground

    // 2. Criar o esquema de cores dinâmico
    val colorScheme = darkColorScheme(
        primary = primaryColor,
        secondary = secondaryColor,
        tertiary = UmbraGold,
        background = backgroundColor,
        surface = UmbraSurface, // Mantemos superfície escura para consistência
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = backgroundColor.toArgb() // A barra de status muda com o tema!
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}