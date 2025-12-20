package com.umbra.umbradex.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.MissionWithProgress
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MissionsUiState(
    val isLoading: Boolean = true,
    val missions: List<MissionWithProgress> = emptyList(),
    val displayedMissions: List<MissionWithProgress> = emptyList(),
    val stats: MissionStats? = null,
    val showAllMissions: Boolean = false,
    val showFilters: Boolean = false,
    val selectedRarity: MissionRarityFilter = MissionRarityFilter.ALL,
    val showCompletedOnly: Boolean = false,
    val showIncompleteOnly: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class MissionsViewModel(
    private val authRepository: AuthRepository,
    private val missionRepository: MissionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MissionsUiState())
    val uiState: StateFlow<MissionsUiState> = _uiState.asStateFlow()

    init {
        loadMissions()
    }

    fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }

            // Load missions with progress
            when (val missionsResult = missionRepository.getMissionsWithProgress(userId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        missions = missionsResult.data ?: emptyList()
                    )

                    // Load statistics
                    loadStats(userId)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = missionsResult.message
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private suspend fun loadStats(userId: String) {
        when (val statsResult = missionRepository.getMissionStats(userId)) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    stats = statsResult.data,
                    isLoading = false
                )
                applyFilters()
            }
            is NetworkResult.Error -> {
                _uiState.value = _uiState.value.copy(isLoading = false)
                applyFilters()
            }
            is NetworkResult.Loading -> {}
        }
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(
            showAllMissions = !_uiState.value.showAllMissions
        )
        applyFilters()
    }

    fun toggleFilters() {
        _uiState.value = _uiState.value.copy(
            showFilters = !_uiState.value.showFilters
        )
    }

    fun selectRarity(rarity: MissionRarityFilter) {
        _uiState.value = _uiState.value.copy(
            selectedRarity = rarity
        )
        applyFilters()
    }

    fun toggleCompletedFilter() {
        _uiState.value = _uiState.value.copy(
            showCompletedOnly = !_uiState.value.showCompletedOnly,
            showIncompleteOnly = false // Disable other filter
        )
        applyFilters()
    }

    fun toggleIncompleteFilter() {
        _uiState.value = _uiState.value.copy(
            showIncompleteOnly = !_uiState.value.showIncompleteOnly,
            showCompletedOnly = false // Disable other filter
        )
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _uiState.value.missions

        // Rarity filter
        _uiState.value.selectedRarity.rarity?.let { rarity ->
            filtered = filtered.filter {
                it.mission.rarity.equals(rarity, ignoreCase = true)
            }
        }

        // Completion status filter
        if (_uiState.value.showCompletedOnly) {
            filtered = filtered.filter { it.isCompleted }
        } else if (_uiState.value.showIncompleteOnly) {
            filtered = filtered.filter { !it.isCompleted }
        }

        // Sort by completion status (incomplete first), then by rarity
        filtered = filtered.sortedWith(
            compareBy<MissionWithProgress> { it.isCompleted }
                .thenBy {
                    when (it.mission.rarity.lowercase()) {
                        "common" -> 0
                        "rare" -> 1
                        "epic" -> 2
                        "legendary" -> 3
                        else -> 4
                    }
                }
        )

        // Limit to 5 if not showing all
        val displayed = if (_uiState.value.showAllMissions) {
            filtered
        } else {
            filtered.take(5)
        }

        _uiState.value = _uiState.value.copy(displayedMissions = displayed)
    }

    fun claimReward(missionId: Int) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            // Check if mission is completed
            val mission = _uiState.value.missions.find { it.mission.id == missionId }
            if (mission?.isCompleted != true) {
                _uiState.value = _uiState.value.copy(
                    error = "Mission not completed yet!"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val claimResult = missionRepository.claimMissionReward(userId, missionId)) {
                is NetworkResult.Success -> {
                    val rewardMission = claimResult.data!!
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Claimed ${rewardMission.goldReward} gold + ${rewardMission.xpReward} XP!"
                    )

                    // Reload missions to update progress
                    loadMissions()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = claimResult.message ?: "Failed to claim reward"
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}