package com.umbra.umbradex.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.data.repository.ShopRepository
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

data class ShopUiState(
    val items: List<ShopItem> = emptyList(),
    val ownedItems: List<String> = emptyList(),
    val userGold: Int = 0,
    val userLevel: Int = 1,
    val equippedSkin: String = "",
    val equippedTheme: String = "",
    val equippedBadge: String = "",
    val equippedTitle: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val selectedRarity: String? = null,
    val purchaseSuccess: String? = null
)

class ShopViewModel : ViewModel() {
    private val shopRepository = ShopRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShopData()
    }

    private fun loadShopData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Carregar perfil do user
            userRepository.getUserProfile().collect { profileResult ->
                if (profileResult is Resource.Success) {
                    val profile = profileResult.data

                    // Carregar itens da loja
                    shopRepository.getAvailableItems().collect { itemsResult ->
                        when (itemsResult) {
                            is Resource.Success -> {
                                // Carregar inventário
                                shopRepository.getUserInventory(profile.id).collect { inventoryResult ->
                                    if (inventoryResult is Resource.Success) {
                                        _uiState.value = _uiState.value.copy(
                                            items = itemsResult.data,
                                            ownedItems = inventoryResult.data,
                                            userGold = profile.gold.toInt(),
                                            userLevel = profile.level,
                                            equippedSkin = profile.equippedSkin,
                                            equippedTheme = profile.equippedTheme,
                                            equippedBadge = profile.equippedBadge,
                                            equippedTitle = profile.equippedTitle,
                                            isLoading = false
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = itemsResult.message
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

    fun purchaseItem(item: ShopItem, userId: String) {
        viewModelScope.launch {
            val currentGold = _uiState.value.userGold

            when (val result = shopRepository.purchaseItem(userId, item, currentGold)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        purchaseSuccess = result.data,
                        userGold = currentGold - item.price,
                        ownedItems = _uiState.value.ownedItems + item.name
                    )
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

    fun filterByRarity(rarity: String?) {
        _uiState.value = _uiState.value.copy(selectedRarity = rarity)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            purchaseSuccess = null
        )
    }
}