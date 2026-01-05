package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class InventoryItem(
    val item: ShopItem,
    val acquiredAt: String
)

class InventoryRepository {

    // Buscar todos os itens do inventário do usuário
    suspend fun getUserInventory(userId: String): Flow<Resource<List<InventoryItem>>> = flow {
        emit(Resource.Loading)
        try {
            // 1. Buscar IDs dos itens no inventário
            val inventoryRecords = UmbraSupabase.client.from("inventory")
                .select(Columns.list("item_id", "category", "acquired_at")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Map<String, String>>()

            // 2. Buscar detalhes dos itens na shop_items
            val itemIds = inventoryRecords.mapNotNull { it["item_id"] }

            if (itemIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val shopItems = UmbraSupabase.client.from("shop_items")
                .select() {
                    filter {
                        // Buscar apenas os itens que o user possui
                        or {
                            itemIds.forEach { itemId ->
                                eq("name", itemId)
                            }
                        }
                    }
                }
                .decodeList<ShopItem>()

            // 3. Combinar os dados
            val inventoryItems = shopItems.mapNotNull { shopItem ->
                val record = inventoryRecords.find { it["item_id"] == shopItem.name }
                record?.let {
                    InventoryItem(
                        item = shopItem,
                        acquiredAt = it["acquired_at"] ?: ""
                    )
                }
            }

            emit(Resource.Success(inventoryItems))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load inventory: ${e.message}", e))
        }
    }

    // Equipar um item
    suspend fun equipItem(
        userId: String,
        itemName: String,
        category: String
    ): Resource<String> {
        return try {
            val updateData = when (category) {
                "skin" -> mapOf("equipped_skin" to itemName)
                "theme" -> mapOf("equipped_theme" to itemName)
                "badge" -> mapOf("equipped_badge" to itemName)
                "name_color" -> mapOf("equipped_name_color" to itemName)
                "title" -> mapOf("equipped_title" to itemName)
                else -> return Resource.Error("Invalid category")
            }

            UmbraSupabase.client.from("profiles")
                .update(updateData) {
                    filter {
                        eq("id", userId)
                    }
                }

            Resource.Success("Item equipped successfully!")
        } catch (e: Exception) {
            Resource.Error("Failed to equip item: ${e.message}", e)
        }
    }

    // Buscar detalhes de um item específico (para preview)
    suspend fun getItemDetails(itemName: String): Resource<ShopItem> {
        return try {
            val item = UmbraSupabase.client.from("shop_items")
                .select() {
                    filter {
                        eq("name", itemName)
                    }
                }
                .decodeSingle<ShopItem>()

            Resource.Success(item)
        } catch (e: Exception) {
            Resource.Error("Failed to load item: ${e.message}", e)
        }
    }
}