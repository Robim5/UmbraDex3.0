package com.umbra.umbradex.ui.missions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.Mission
import com.umbra.umbradex.data.model.MissionProgress
import com.umbra.umbradex.data.model.UserProfile // <--- Importante!
import com.umbra.umbradex.data.repository.MissionRepository
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow



data class MissionWithProgress(
    val mission: Mission,
    val progress: MissionProgress?,
    val progressPercentage: Float,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    val canClaim: Boolean
)

data class MissionsUiState(
    val missions: List<MissionWithProgress> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val successMessage: String? = null,
    val userGold: Int = 0,
    val userXp: Int = 0,
    val userLevel: Int = 1
)

class MissionsViewModel : ViewModel() {
    private val missionRepository = MissionRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(MissionsUiState())
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    fun loadMissions(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Carregar perfil do usuário
            userRepository.getUserProfile().collect { profileResult ->
                if (profileResult is Resource.Success) {
                    val profile = profileResult.data

                    // Carregar missões
                    missionRepository.getAllMissions().collect { missionsResult ->
                        if (missionsResult is Resource.Success) {
                            // Carregar progresso
                            missionRepository.getUserMissionProgress(userId).collect { progressResult ->
                                if (progressResult is Resource.Success) {
                                    val combinedMissions = combineData(
                                        missionsResult.data,
                                        progressResult.data
                                    )

                                    _uiState.value = _uiState.value.copy(
                                        missions = combinedMissions,
                                        userGold = profile.gold.toInt(),
                                        userXp = profile.xp.toInt(),
                                        userLevel = profile.level,
                                        isLoading = false
                                    )
                                }
                            }
                        } else if (missionsResult is Resource.Error) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = missionsResult.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun combineData(
        missions: List<Mission>,
        progressList: List<MissionProgress>
    ): List<MissionWithProgress> {
        val progressMap = progressList.associateBy { it.missionId }

        return missions.map { mission ->
            val progress = progressMap[mission.id]
            val currentValue = progress?.currentValue ?: 0
            val status = progress?.status ?: "locked"

            val progressPercentage = if (mission.requirementValue > 0) {
                (currentValue.toFloat() / mission.requirementValue.toFloat()).coerceIn(0f, 1f)
            } else 0f

            val isCompleted = status == "completed"
            val isLocked = status == "locked"
            val canClaim = status == "active" && currentValue >= mission.requirementValue

            MissionWithProgress(
                mission = mission,
                progress = progress,
                progressPercentage = progressPercentage,
                isCompleted = isCompleted,
                isLocked = isLocked,
                canClaim = canClaim
            )
        }
    }

    fun claimReward(userId: String, missionId: Int) {
        viewModelScope.launch {
            when (val result = missionRepository.claimMissionReward(userId, missionId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = result.data
                    )
                    // Recarregar missões
                    loadMissions(userId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}