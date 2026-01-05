package com.umbra.umbradex.data.repository

import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.supabase.UmbraSupabase
import com.umbra.umbradex.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow




class ShopRepository {

    // Buscar todos os itens disponíveis na loja
    suspend fun getAvailableItems(): Flow<Resource<List<ShopItem>>> = flow {
        emit(Resource.Loading)
        try {
            val items = UmbraSupabase.client.from("shop_items")
                .select()
                .decodeList<ShopItem>()
                .filter { it.isAvailable }
                .sortedBy { it.sortOrder }

            emit(Resource.Success(items))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load shop items: ${e.message}", e))
        }
    }

    // Verificar se o user já possui um item
    suspend fun userOwnsItem(userId: String, itemName: String, category: String): Boolean {
        return try {
            val result = UmbraSupabase.client.from("inventory")
                .select(Columns.list("id")) {
                    filter {
                        eq("user_id", userId)
                        eq("item_id", itemName)
                        eq("category", category)
                    }
                }
                .decodeList<Map<String, Any>>()

            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // Comprar item (gastar gold + adicionar ao inventário)
    suspend fun purchaseItem(
        userId: String,
        item: ShopItem,
        currentGold: Int
    ): Resource<String> {
        return try {
            // 1. Verificar se tem gold suficiente
            if (currentGold < item.price) {
                return Resource.Error("Insufficient gold")
            }

            // 2. Verificar se já possui
            if (userOwnsItem(userId, item.name, item.type)) {
                return Resource.Error("Item already owned")
            }

            // 3. Gastar gold usando RPC
            UmbraSupabase.client.from("rpc")
                .select {
                    filter {
                        eq("spend_gold", mapOf(
                            "p_user_id" to userId,
                            "p_amount" to item.price
                        ))
                    }
                }

            // 4. Adicionar ao inventário
            UmbraSupabase.client.from("inventory").insert(
                mapOf(
                    "user_id" to userId,
                    "item_id" to item.name,
                    "category" to item.type
                )
            )

            Resource.Success("Item purchased successfully!")
        } catch (e: Exception) {
            if (e.message?.contains("Insufficient gold") == true) {
                Resource.Error("Not enough gold!")
            } else {
                Resource.Error("Purchase failed: ${e.message}", e)
            }
        }
    }

    // Buscar itens do inventário do user
    suspend fun getUserInventory(userId: String): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading)
        try {
            val inventory = UmbraSupabase.client.from("inventory")
                .select(Columns.list("item_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["item_id"] }

            emit(Resource.Success(inventory))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load inventory: ${e.message}", e))
        }
    }
}