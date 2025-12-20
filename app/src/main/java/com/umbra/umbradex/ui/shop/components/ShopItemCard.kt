package com.umbra.umbradex.ui.shop.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umbra.umbradex.R
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val rarityColor = getRarityColor(item.rarity)
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isOwned -> SuccessColor.copy(alpha = 0.1f)
            !canAfford -> ErrorColor.copy(alpha = 0.1f)
            else -> PurpleSurface
        },
        animationSpec = tween(300),
        label = "background_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = !isOwned && canAfford) {
                isPressed = true
                onClick()
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(100)
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isOwned) 2.dp else 4.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Gradient background based on rarity
            if (!isOwned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    rarityColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isOwned)
                                SuccessColor.copy(alpha = 0.2f)
                            else
                                rarityColor.copy(alpha = 0.2f)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isOwned) SuccessColor else rarityColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (item.type) {
                        "avatar" -> {
                            // Show avatar image
                            item.assetUrl?.let { url ->
                                // For local assets, map URL to drawable
                                val drawableId = getDrawableFromUrl(url)
                                if (drawableId != null) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(40.dp),
                                        tint = if (isOwned) SuccessColor else rarityColor
                                    )
                                }
                            }
                        }
                        "badge" -> {
                            item.assetUrl?.let { url ->
                                val drawableId = getDrawableFromUrl(url)
                                if (drawableId != null) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = item.name,
                                        modifier = Modifier.size(50.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(40.dp),
                                        tint = if (isOwned) SuccessColor else rarityColor
                                    )
                                }
                            }
                        }
                        "theme" -> {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = item.name,
                                modifier = Modifier.size(40.dp),
                                tint = if (isOwned) SuccessColor else rarityColor
                            )
                        }
                        "name_color" -> {
                            // Show color preview
                            item.colorHex?.let { colorHex ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(colorHex)))
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = item.name,
                                modifier = Modifier.size(40.dp),
                                tint = if (isOwned) SuccessColor else rarityColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Item info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Rarity badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = rarityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.rarity.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rarityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item name
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Item type
                    Text(
                        text = when (item.type) {
                            "avatar" -> "Avatar"
                            "badge" -> "Badge"
                            "theme" -> "Theme"
                            "name_color" -> "Name Color"
                            else -> "Item"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Price or status
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (isOwned) {
                        // Owned badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Owned",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "OWNED",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Price
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (canAfford)
                                    LegendaryColor.copy(alpha = 0.2f)
                                else
                                    ErrorColor.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Gold",
                                    tint = if (canAfford) LegendaryColor else ErrorColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = item.price.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) LegendaryColor else ErrorColor
                                )
                            }
                        }

                        if (!canAfford) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Not enough gold",
                                style = MaterialTheme.typography.labelSmall,
                                color = ErrorColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to map asset URLs to drawable resources
private fun getDrawableFromUrl(url: String): Int? {
    return when {
        // Standard avatars (from onboarding)
        url.contains("male1") -> R.drawable.standard_male1
        url.contains("male2") -> R.drawable.standard_male2
        url.contains("male3") -> R.drawable.standard_male3
        url.contains("male4") -> R.drawable.standard_male4
        url.contains("male5") -> R.drawable.standard_male5
        url.contains("female1") -> R.drawable.standard_female1
        url.contains("female2") -> R.drawable.standard_female2
        url.contains("female3") -> R.drawable.standard_female3
        url.contains("female4") -> R.drawable.standard_female4
        url.contains("female5") -> R.drawable.standard_female5

        // Shop avatars - Common
        url.contains("fightboy") -> R.drawable.shop_common_fightboy
        url.contains("girldance") -> R.drawable.shop_common_girldance
        url.contains("greenhair") -> R.drawable.shop_common_greenhair

        // Shop avatars - Rare
        url.contains("artistboy") -> R.drawable.shop_rare_artistboy

        // Shop avatars - Epic
        url.contains("purplegirl") -> R.drawable.shop_epic_purplegirl
        url.contains("waterguy") -> R.drawable.shop_epic_waterguy

        // Shop avatars - Legendary
        url.contains("dreamgirl") -> R.drawable.shop_legendary_dreamgirl
        url.contains("ninjaboy") -> R.drawable.shop_legendary_ninjaboy
        url.contains("oldguy") -> R.drawable.shop_legendary_oldguy

        // Badges - Common
        url.contains("balance") -> R.drawable.shop_common_balance
        url.contains("feather") -> R.drawable.shop_common_feather
        url.contains("rain") -> R.drawable.shop_common_rain
        url.contains("rock") -> R.drawable.shop_common_rock

        // Badges - Rare
        url.contains("fire") -> R.drawable.shop_rare_fire
        url.contains("leaf") -> R.drawable.shop_rare_leaf
        url.contains("petal") -> R.drawable.shop_rare_petal
        url.contains("water") -> R.drawable.shop_rare_water

        // Badges - Epic
        url.contains("ghost") -> R.drawable.shop_epic_ghost
        url.contains("ice") -> R.drawable.shop_epic_ice
        url.contains("sunflower") -> R.drawable.shop_epic_sunflower

        // Badges - Legendary
        url.contains("gold") -> R.drawable.shop_legendary_gold
        url.contains("hearth") -> R.drawable.shop_legendary_hearth
        url.contains("rainbow") -> R.drawable.shop_legendary_rainbow
        url.contains("demon") -> R.drawable.shop_legendary_demon

        // Start badge
        url.contains("start_badget") -> R.drawable.start_badget

        else -> null
    }
}