package com.umbra.umbradex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.data.repository.DataRepository
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val dataRepository = DataRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // Estado para tracking de cliques no pet
    private val _petClickCount = MutableStateFlow(0)
    val petClickCount: StateFlow<Int> = _petClickCount

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                var userProfile: UserProfile? = null
                var livingDexStats: Map<String, Any>? = null
                var missionStats: Map<String, Int>? = null
                var totalTime: Long = 0L

                // Carregar Perfil
                userRepository.getUserProfile().collect { res ->
                    if (res is Resource.Success) userProfile = res.data
                }

                // Carregar Stats Living Dex
                dataRepository.getLivingDexStats().collect { res ->
                    if (res is Resource.Success) livingDexStats = res.data
                }

                // Carregar Stats Missões
                dataRepository.getMissionStats().collect { res ->
                    if (res is Resource.Success) missionStats = res.data
                }

                // Carregar Tempo Total
                dataRepository.getTotalTimeOnline().collect { res ->
                    if (res is Resource.Success) totalTime = res.data
                }

                if (userProfile != null && livingDexStats != null && missionStats != null) {
                    _uiState.value = HomeUiState.Success(
                        profile = userProfile!!,
                        pokedexCaught = livingDexStats!!["total_caught"] as Int,
                        pokedexTotal = livingDexStats!!["total_possible"] as Int,
                        missionsCompleted = missionStats!!["completed"] ?: 0,
                        missionsTotal = missionStats!!["total"] ?: 200,
                        totalTimeSeconds = totalTime
                    )
                } else {
                    _uiState.value = HomeUiState.Error("Não foi possível carregar os dados.")
                }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun onPetClick() {
        _petClickCount.value += 1

        // Incrementa no backend
        viewModelScope.launch {
            userRepository.incrementPetClicks()
        }

        // Reset após 3 cliques (para a rotação)
        if (_petClickCount.value >= 3) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000) // Espera a animação
                _petClickCount.value = 0
            }
        }
    }

    fun getRankTitle(completionPercentage: Float): String {
        return when {
            completionPercentage >= 0.85f -> "Imperador Pokémon"
            completionPercentage >= 0.50f -> "Campeão"
            completionPercentage >= 0.25f -> "Gym King"
            completionPercentage >= 0.10f -> "Sonhador"
            else -> "Explorador do Novo Mundo"
        }
    }

    fun getRankColor(completionPercentage: Float): androidx.compose.ui.graphics.Color {
        return when {
            completionPercentage >= 0.85f -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Gold
            completionPercentage >= 0.50f -> androidx.compose.ui.graphics.Color(0xFFC0C0C0) // Silver
            completionPercentage >= 0.25f -> androidx.compose.ui.graphics.Color(0xFFCD7F32) // Bronze
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val profile: UserProfile,
        val pokedexCaught: Int,
        val pokedexTotal: Int,
        val missionsCompleted: Int,
        val missionsTotal: Int,
        val totalTimeSeconds: Long
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}