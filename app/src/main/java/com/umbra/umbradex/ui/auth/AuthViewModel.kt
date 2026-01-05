package com.umbra.umbradex.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.supabase.UmbraSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.umbra.umbradex.data.repository.AuthRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // Estados de autenticação
    private val _authState = MutableStateFlow<Resource<Boolean>>(Resource.Loading)
    val authState: StateFlow<Resource<Boolean>> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estados para navegação
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Dados do onboarding
    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _isAuthenticated.value = authRepository.isUserLoggedIn()
        if (_isAuthenticated.value) {
            // Buscar ID do usuário atual
            viewModelScope.launch {
                val userId = authRepository.getCurrentUserId()
                _currentUserId.value = userId
            }
        }
    }

    // Login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.login(email, password).collect { result ->
                _authState.value = result
                _isLoading.value = false

                if (result is Resource.Success) {
                    _isAuthenticated.value = true
                    _currentUserId.value = authRepository.getCurrentUserId()
                }
            }
        }
    }

    // Signup (após completar onboarding)
    fun signup() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = _onboardingData.value

            authRepository.signup(
                email = data.email,
                password = data.password,
                username = data.username,
                birthDate = data.birthDate,
                pokemonKnowledge = data.pokemonKnowledge,
                favoriteType = data.favoriteType,
                avatar = data.avatar,
                starterId = data.starterId
            ).collect { result ->
                _authState.value = result
                _isLoading.value = false

                if (result is Resource.Success) {
                    _isAuthenticated.value = true
                    _currentUserId.value = authRepository.getCurrentUserId()
                }
            }
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isAuthenticated.value = false
            _currentUserId.value = null
            _authState.value = Resource.Loading
        }
    }

    // Atualizar dados do onboarding
    fun updateOnboardingData(update: OnboardingData.() -> OnboardingData) {
        _onboardingData.value = _onboardingData.value.update()
    }

    // Reset do estado
    fun resetAuthState() {
        _authState.value = Resource.Loading
    }
}

// Data class para dados do onboarding
data class OnboardingData(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val birthDate: String = "",
    val pokemonKnowledge: String = "intermediate",
    val favoriteType: String = "fire",
    val avatar: String = "standard_male1",
    val starterId: Int = 1,
    val currentStep: Int = 0
)