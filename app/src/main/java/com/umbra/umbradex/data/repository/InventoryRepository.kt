package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.UserInventory
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.model.EquippedItems
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class InventoryRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get all user inventory items
    suspend fun getUserInventory(userId: String): NetworkResult<List<UserInventory>> {
        return try {
            val result = supabase.from("user_inventory")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<UserInventory>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch inventory")
        }
    }

    // Get user inventory by type
    suspend fun getUserInventoryByType(userId: String, type: String): NetworkResult<List<UserInventory>> {
        return try {
            // First get all inventory items
            val inventory = getUserInventory(userId)
            if (inventory !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch inventory")
            }

            // Then filter by getting shop items details
            val itemIds = inventory.data!!.map { it.itemId }
            val shopItems = supabase.from("shop_items")
                .select {
                    filter {
                        isIn("id", itemIds)
                        eq("type", type)
                    }
                }
                .decodeList<ShopItem>()

            val shopItemIds = shopItems.map { it.id }
            val filtered = inventory.data.filter { it.itemId in shopItemIds }

            NetworkResult.Success(filtered)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch inventory by type")
        }
    }

    // Add item to inventory (purchase)
    suspend fun purchaseItem(userId: String, itemId: Int): NetworkResult<UserInventory> {
        return try {
            val item = buildJsonObject {
                put("user_id", userId)
                put("item_id", itemId)
                put("is_equipped", false)
            }

            val result = supabase.from("user_inventory")
                .insert(item)
                .decodeSingle<UserInventory>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to purchase item")
        }
    }

    // Equip item
    suspend fun equipItem(userId: String, itemId: Int, itemType: String): NetworkResult<Unit> {
        return try {
            // First unequip all items of the same type
            val currentInventory = getUserInventoryByType(userId, itemType)
            if (currentInventory is NetworkResult.Success) {
                currentInventory.data!!.forEach { inventoryItem ->
                    if (inventoryItem.isEquipped) {
                        unequipItem(userId, inventoryItem.itemId)
                    }
                }
            }

            // Then equip the new item
            supabase.from("user_inventory")
                .update(buildJsonObject {
                    put("is_equipped", true)
                }) {
                    filter {
                        eq("user_id", userId)
                        eq("item_id", itemId)
                    }
                }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to equip item")
        }
    }

    // Unequip item
    suspend fun unequipItem(userId: String, itemId: Int): NetworkResult<Unit> {
        return try {
            supabase.from("user_inventory")
                .update(buildJsonObject {
                    put("is_equipped", false)
                }) {
                    filter {
                        eq("user_id", userId)
                        eq("item_id", itemId)
                    }
                }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to unequip item")
        }
    }

    // Get equipped items
    suspend fun getEquippedItems(userId: String): NetworkResult<EquippedItems> {
        return try {
            val inventory = supabase.from("user_inventory")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_equipped", true)
                    }
                }
                .decodeList<UserInventory>()

            // Get shop item details for equipped items
            val itemIds = inventory.map { it.itemId }
            if (itemIds.isEmpty()) {
                return NetworkResult.Success(EquippedItems())
            }

            val shopItems = supabase.from("shop_items")
                .select {
                    filter {
                        isIn("id", itemIds)
                    }
                }
                .decodeList<ShopItem>()

            val avatar = shopItems.firstOrNull { it.type == "avatar" }
            val badge = shopItems.firstOrNull { it.type == "badge" }
            val nameColor = shopItems.firstOrNull { it.type == "name_color" }
            val theme = shopItems.firstOrNull { it.type == "theme" }

            NetworkResult.Success(
                EquippedItems(
                    avatar = avatar,
                    badge = badge,
                    nameColor = nameColor,
                    theme = theme
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch equipped items")
        }
    }

    // Check if user owns item
    suspend fun ownsItem(userId: String, itemId: Int): NetworkResult<Boolean> {
        return try {
            val result = supabase.from("user_inventory")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("item_id", itemId)
                    }
                }
                .decodeList<UserInventory>()

            NetworkResult.Success(result.isNotEmpty())
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to check item ownership")
        }
    }

    // Get inventory count by rarity
    suspend fun getInventoryCountByRarity(userId: String): NetworkResult<Map<String, Int>> {
        return try {
            val inventory = getUserInventory(userId)
            if (inventory !is NetworkResult.Success) {
                return NetworkResult.Error("Failed to fetch inventory")
            }

            val itemIds = inventory.data!!.map { it.itemId }
            val shopItems = supabase.from("shop_items")
                .select {
                    filter {
                        isIn("id", itemIds)
                    }
                }
                .decodeList<ShopItem>()

            val counts = shopItems.groupingBy { it.rarity }.eachCount()

            NetworkResult.Success(counts)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to get inventory counts")
        }
    }
}