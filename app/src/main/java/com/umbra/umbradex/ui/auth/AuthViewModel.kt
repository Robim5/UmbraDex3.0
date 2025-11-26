package com.umbra.umbradex.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.local.PreferencesManager
import com.umbra.umbradex.data.model.OnboardingData
import com.umbra.umbradex.data.repository.AuthRepository
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
    object SignUpSuccess : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession.asStateFlow()

    init {
        checkExistingSession()
    }

    // Check if user is already logged in
    private fun checkExistingSession() {
        viewModelScope.launch {
            _isCheckingSession.value = true

            // Check if user is logged in with Supabase
            val isLoggedIn = authRepository.isLoggedIn()

            if (isLoggedIn) {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    // Save session to preferences
                    preferencesManager.saveUserSession(userId, "")
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }

            _isCheckingSession.value = false
        }
    }

    // Login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    val userId = authRepository.getCurrentUserId()
                    if (userId != null) {
                        preferencesManager.saveUserSession(userId, email)
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Failed to get user ID")
                    }
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "Login failed")
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }

    // Sign up (creates auth account only)
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.signUp(email, password)) {
                is NetworkResult.Success -> {
                    // After signup, automatically login
                    when (val loginResult = authRepository.login(email, password)) {
                        is NetworkResult.Success -> {
                            _authState.value = AuthState.SignUpSuccess
                        }
                        is NetworkResult.Error -> {
                            _authState.value = AuthState.Error(loginResult.message ?: "Auto-login failed")
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "Sign up failed")
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }

    // Complete profile (after onboarding)
    fun completeOnboarding(onboardingData: OnboardingData) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.createProfile(onboardingData)) {
                is NetworkResult.Success -> {
                    val userId = authRepository.getCurrentUserId()
                    if (userId != null) {
                        preferencesManager.saveUserSession(userId, "")
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Failed to get user ID")
                    }
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "Failed to create profile")
                }
                is NetworkResult.Loading -> {
                    // Already loading
                }
            }
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            preferencesManager.clearSession()
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Reset state
    fun resetState() {
        _authState.value = AuthState.Unauthenticated
    }
}