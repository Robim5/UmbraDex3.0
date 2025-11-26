package com.umbra.umbradex.ui.auth

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.umbra.umbradex.data.local.PreferencesManager
import com.umbra.umbradex.data.repository.AuthRepository
import com.umbra.umbradex.data.repository.UserRepository

enum class AuthScreen {
    LOGIN,
    SIGNUP,
    ONBOARDING
}

@Composable
fun AuthContainer(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val userRepository = remember { UserRepository() }
    val preferencesManager = remember { PreferencesManager(context) }

    val viewModel = remember {
        AuthViewModel(authRepository, userRepository, preferencesManager)
    }

    val authState by viewModel.authState.collectAsState()
    val isCheckingSession by viewModel.isCheckingSession.collectAsState()

    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    // Navigate based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onAuthSuccess()
            }
            is AuthState.SignUpSuccess -> {
                currentScreen = AuthScreen.ONBOARDING
            }
            else -> {}
        }
    }

    // Show loading while checking session
    if (isCheckingSession) {
        // You can show a splash screen here
        return
    }

    when (currentScreen) {
        AuthScreen.LOGIN -> {
            LoginScreen(
                authState = authState,
                onLoginClick = { email, password ->
                    viewModel.login(email, password)
                },
                onSignUpClick = {
                    currentScreen = AuthScreen.SIGNUP
                    viewModel.resetState()
                }
            )
        }

        AuthScreen.SIGNUP -> {
            SignUpScreen(
                authState = authState,
                onSignUpClick = { email, password ->
                    viewModel.signUp(email, password)
                },
                onBackClick = {
                    currentScreen = AuthScreen.LOGIN
                    viewModel.resetState()
                }
            )
        }

        AuthScreen.ONBOARDING -> {
            OnboardingScreen(
                authState = authState,
                onComplete = { onboardingData ->
                    viewModel.completeOnboarding(onboardingData)
                }
            )
        }
    }
}