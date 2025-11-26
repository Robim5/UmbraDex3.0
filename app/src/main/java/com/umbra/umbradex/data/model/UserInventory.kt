package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInventory(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("item_id")
    val itemId: Int,
    @SerialName("is_equipped")
    val isEquipped: Boolean = false,
    @SerialName("purchased_at")
    val purchasedAt: String? = null
)

// For displaying inventory with full item data
data class InventoryItemWithDetails(
    val inventoryItem: UserInventory,
    val shopItem: ShopItem
)

// For equipped items display
data class EquippedItems(
    val avatar: ShopItem? = null,
    val badge: ShopItem? = null,
    val nameColor: ShopItem? = null,
    val theme: ShopItem? = null
)