package com.umbra.umbradex.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShopUiState(
    val isLoading: Boolean = true,
    val shopItems: List<ShopItem> = emptyList(),
    val displayedItems: List<ShopItem> = emptyList(),
    val ownedItemIds: Set<Int> = emptySet(),
    val userGold: Int = 0,
    val selectedCategory: ShopCategory = ShopCategory.ALL,
    val selectedCategoryIndex: Int = 0,
    val selectedRarity: RarityFilter = RarityFilter.ALL,
    val selectedItem: ShopItem? = null,
    val showPurchaseDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ShopViewModel(
    private val authRepository: AuthRepository,
    private val shopRepository: ShopRepository,
    private val inventoryRepository: InventoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShopData()
    }

    private fun loadShopData() {
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

            // Load user profile for gold
            when (val userResult = userRepository.getUserProfile(userId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        userGold = userResult.data?.gold ?: 0
                    )
                }
                is NetworkResult.Error -> {
                    println("Failed to load user gold: ${userResult.message}")
                }
                is NetworkResult.Loading -> {}
            }

            // Load all shop items
            when (val shopResult = shopRepository.getAllShopItems()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        shopItems = shopResult.data ?: emptyList()
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = shopResult.message
                    )
                    return@launch
                }
                is NetworkResult.Loading -> {}
            }

            // Load user inventory (owned items)
            when (val inventoryResult = inventoryRepository.getUserInventory(userId)) {
                is NetworkResult.Success -> {
                    val ownedIds = inventoryResult.data?.map { it.itemId }?.toSet() ?: emptySet()
                    _uiState.value = _uiState.value.copy(
                        ownedItemIds = ownedIds,
                        isLoading = false
                    )
                    applyFilters()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = inventoryResult.message
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun selectCategory(category: ShopCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedCategoryIndex = ShopCategory.entries.indexOf(category)
        )
        applyFilters()
    }

    fun selectRarity(rarity: RarityFilter) {
        _uiState.value = _uiState.value.copy(selectedRarity = rarity)
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _uiState.value.shopItems

        // Filter by category
        if (_uiState.value.selectedCategory != ShopCategory.ALL) {
            filtered = filtered.filter {
                it.type == _uiState.value.selectedCategory.type
            }
        }

        // Filter by rarity
        if (_uiState.value.selectedRarity != RarityFilter.ALL) {
            filtered = filtered.filter {
                it.rarity.equals(_uiState.value.selectedRarity.rarity, ignoreCase = true)
            }
        }

        // Sort by rarity (legendary first) and then by price
        filtered = filtered.sortedWith(
            compareByDescending<ShopItem> { item ->
                when (item.rarity.lowercase()) {
                    "legendary" -> 4
                    "epic" -> 3
                    "rare" -> 2
                    "common" -> 1
                    else -> 0
                }
            }.thenBy { it.price }
        )

        _uiState.value = _uiState.value.copy(displayedItems = filtered)
    }

    fun showPurchaseDialog(item: ShopItem) {
        _uiState.value = _uiState.value.copy(
            selectedItem = item,
            showPurchaseDialog = true
        )
    }

    fun dismissPurchaseDialog() {
        _uiState.value = _uiState.value.copy(
            selectedItem = null,
            showPurchaseDialog = false
        )
    }

    fun confirmPurchase() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val item = _uiState.value.selectedItem ?: return@launch

            // Check if user can afford
            if (_uiState.value.userGold < item.price) {
                _uiState.value = _uiState.value.copy(
                    error = "Not enough gold!",
                    showPurchaseDialog = false,
                    selectedItem = null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            // Purchase item
            when (val purchaseResult = inventoryRepository.purchaseItem(userId, item.id)) {
                is NetworkResult.Success -> {
                    // Deduct gold
                    val newGold = _uiState.value.userGold - item.price
                    when (userRepository.updateGold(userId, newGold)) {
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                userGold = newGold,
                                ownedItemIds = _uiState.value.ownedItemIds + item.id,
                                successMessage = "Successfully purchased ${item.name}!",
                                showPurchaseDialog = false,
                                selectedItem = null,
                                isLoading = false
                            )
                            applyFilters()
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to update gold",
                                showPurchaseDialog = false,
                                selectedItem = null,
                                isLoading = false
                            )
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = purchaseResult.message ?: "Failed to purchase item",
                        showPurchaseDialog = false,
                        selectedItem = null,
                        isLoading = false
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

    fun refresh() {
        loadShopData()
    }
}