package com.umbra.umbradex.data.repository

import com.umbra.umbradex.UmbraDexApplication
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.util.NetworkResult
import io.github.jan.supabase.postgrest.from

class ShopRepository {

    private val supabase = UmbraDexApplication.supabase

    // Get all shop items
    suspend fun getAllShopItems(): NetworkResult<List<ShopItem>> {
        return try {
            val result = supabase.from("shop_items")
                .select()
                .decodeList<ShopItem>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch shop items")
        }
    }

    // Get shop items by type
    suspend fun getShopItemsByType(type: String): NetworkResult<List<ShopItem>> {
        return try {
            val result = supabase.from("shop_items")
                .select {
                    filter {
                        eq("type", type)
                    }
                }
                .decodeList<ShopItem>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch shop items by type")
        }
    }

    // Get shop items by rarity
    suspend fun getShopItemsByRarity(rarity: String): NetworkResult<List<ShopItem>> {
        return try {
            val result = supabase.from("shop_items")
                .select {
                    filter {
                        eq("rarity", rarity)
                    }
                }
                .decodeList<ShopItem>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch shop items by rarity")
        }
    }

    // Get shop items by type AND rarity
    suspend fun getShopItemsByTypeAndRarity(type: String, rarity: String): NetworkResult<List<ShopItem>> {
        return try {
            val result = supabase.from("shop_items")
                .select {
                    filter {
                        eq("type", type)
                        eq("rarity", rarity)
                    }
                }
                .decodeList<ShopItem>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch shop items")
        }
    }

    // Get single shop item
    suspend fun getShopItem(itemId: Int): NetworkResult<ShopItem> {
        return try {
            val result = supabase.from("shop_items")
                .select {
                    filter {
                        eq("id", itemId)
                    }
                }
                .decodeSingle<ShopItem>()

            NetworkResult.Success(result)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to fetch shop item")
        }
    }

    // Get avatars (for shop display)
    suspend fun getAvatars(): NetworkResult<List<ShopItem>> {
        return getShopItemsByType("avatar")
    }

    // Get themes (for shop display)
    suspend fun getThemes(): NetworkResult<List<ShopItem>> {
        return getShopItemsByType("theme")
    }

    // Get badges (for shop display)
    suspend fun getBadges(): NetworkResult<List<ShopItem>> {
        return getShopItemsByType("badge")
    }

    // Get name colors (for shop display)
    suspend fun getNameColors(): NetworkResult<List<ShopItem>> {
        return getShopItemsByType("name_color")
    }
}