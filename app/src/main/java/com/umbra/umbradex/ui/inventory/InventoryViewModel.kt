package com.umbra.umbradex.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.umbra.umbradex.data.repository.InventoryRepository
import kotlinx.coroutines.flow.StateFlow

data class EquippedItems(
    val skin: ShopItem? = null,
    val theme: ShopItem? = null,
    val badge: ShopItem? = null,
    val nameColor: ShopItem? = null,
    val title: String = "Rookie",
    val partnerPokemonId: Int? = null
)

data class InventoryUiState(
    val inventoryItems: List<com.umbra.umbradex.data.repository.InventoryItem> = emptyList(),
    val equippedItems: EquippedItems = EquippedItems(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "skin",
    val successMessage: String? = null
)

class InventoryViewModel : ViewModel() {
    private val inventoryRepository = InventoryRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    fun loadInventory(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Carregar perfil do usuário para saber o que está equipado
            userRepository.getUserProfile().collect { profileResult ->
                if (profileResult is Resource.Success) {
                    val profile = profileResult.data

                    // Carregar detalhes dos itens equipados
                    val equippedSkin = if (profile.equippedSkin.isNotEmpty()) {
                        inventoryRepository.getItemDetails(profile.equippedSkin).let {
                            if (it is Resource.Success) it.data else null
                        }
                    } else null

                    val equippedTheme = if (profile.equippedTheme.isNotEmpty()) {
                        inventoryRepository.getItemDetails(profile.equippedTheme).let {
                            if (it is Resource.Success) it.data else null
                        }
                    } else null

                    val equippedBadge = if (profile.equippedBadge.isNotEmpty()) {
                        inventoryRepository.getItemDetails(profile.equippedBadge).let {
                            if (it is Resource.Success) it.data else null
                        }
                    } else null

                    // Carregar inventário
                    inventoryRepository.getUserInventory(userId).collect { inventoryResult ->
                        when (inventoryResult) {
                            is Resource.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    inventoryItems = inventoryResult.data,
                                    equippedItems = EquippedItems(
                                        skin = equippedSkin,
                                        theme = equippedTheme,
                                        badge = equippedBadge,
                                        nameColor = null, // TODO: Implementar se necessário
                                        title = profile.equippedTitle,
                                        partnerPokemonId = profile.equippedPokemonId
                                    ),
                                    isLoading = false
                                )
                            }
                            is Resource.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = inventoryResult.message
                                )
                            }
                            is Resource.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun equipItem(userId: String, itemName: String, category: String) {
        viewModelScope.launch {
            when (val result = inventoryRepository.equipItem(userId, itemName, category)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = result.data
                    )
                    // Recarregar inventário para atualizar estado
                    loadInventory(userId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}