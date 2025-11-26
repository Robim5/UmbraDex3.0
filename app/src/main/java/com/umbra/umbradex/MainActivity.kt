package com.umbra.umbradex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.umbra.umbradex.ui.theme.UmbraDexTheme
import androidx.compose.runtime.*
import com.umbra.umbradex.ui.auth.AuthContainer
import com.umbra.umbradex.ui.splash.SplashScreen
import com.umbra.umbradex.ui.navigation.MainNavigation


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UmbraDexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    var isAuthenticated by remember { mutableStateOf(false) }

                    when {
                        showSplash -> {
                            SplashScreen(
                                onSplashComplete = {
                                    showSplash = false
                                }
                            )
                        }

                        isAuthenticated -> {
                            MainNavigation(
                                onLogout = {
                                    isAuthenticated = false
                                }
                            )
                        }

                        else -> {
                            AuthContainer(
                                onAuthSuccess = {
                                    isAuthenticated = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}