// Em: app/src/main/java/com/umbra/umbradex/MainActivity.kt
package com.umbra.umbradex // Certifica-te que o package está correto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.umbra.umbradex.ui.theme.UmbraDexTheme // Certifica-te que tens os imports corretos
import com.umbra.umbradex.ui.navigation.UmbraNavGraph // E este também

class MainActivity : ComponentActivity() {

    // Cria ou obtém o ViewModel existente
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Recolhe o StateFlow como estado do Compose
            val themeColors by viewModel.themeColors.collectAsState()

            UmbraDexTheme(themeColors = themeColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    UmbraNavGraph(navController = navController)
                }
            }
        }
    }
}
