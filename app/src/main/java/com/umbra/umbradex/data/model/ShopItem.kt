package com.umbra.umbradex.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopItem(
    val id: Int,
    val type: String, // "theme", "avatar", "badge", "name_color"
    val name: String,
    val rarity: String, // "common", "rare", "epic", "legendary"
    val price: Int,
    @SerialName("asset_url")
    val assetUrl: String? = null,
    @SerialName("color_hex")
    val colorHex: String? = null // For name colors
)

enum class ShopItemType {
    THEME,
    AVATAR,
    BADGE,
    NAME_COLOR;

    companion object {
        fun fromString(type: String): ShopItemType {
            return when (type.lowercase()) {
                "theme" -> THEME
                "avatar" -> AVATAR
                "badge" -> BADGE
                "name_color" -> NAME_COLOR
                else -> AVATAR
            }
        }
    }
}

enum class Rarity(val displayName: String, val color: Long) {
    COMMON("Common", 0xFF9E9E9E),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFFFD700);

    companion object {
        fun fromString(rarity: String): Rarity {
            return when (rarity.lowercase()) {
                "common" -> COMMON
                "rare" -> RARE
                "epic" -> EPIC
                "legendary" -> LEGENDARY
                else -> COMMON
            }
        }
    }
}