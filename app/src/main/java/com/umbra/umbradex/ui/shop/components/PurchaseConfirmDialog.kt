package com.umbra.umbradex.ui.shop.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.umbra.umbradex.R
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.ui.theme.*

@Composable
fun PurchaseConfirmDialog(
    item: ShopItem,
    userGold: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showAnimation = true
    }

    val rarityColor = getRarityColor(item.rarity)
    val canAfford = userGold >= item.price

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = showAnimation,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PurpleSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        rarityColor.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            text = "Purchase Item?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Item preview
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(rarityColor.copy(alpha = 0.2f))
                                .border(
                                    width = 3.dp,
                                    color = rarityColor,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when (item.type) {
                                "avatar" -> {
                                    item.assetUrl?.let { url ->
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
                                                modifier = Modifier.size(60.dp),
                                                tint = rarityColor
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
                                                modifier = Modifier.size(80.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = item.name,
                                                modifier = Modifier.size(60.dp),
                                                tint = rarityColor
                                            )
                                        }
                                    }
                                }
                                "theme" -> {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(60.dp),
                                        tint = rarityColor
                                    )
                                }
                                "name_color" -> {
                                    item.colorHex?.let { colorHex ->
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                                .border(3.dp, Color.White, CircleShape)
                                        )
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(60.dp),
                                        tint = rarityColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rarity badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = rarityColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = item.rarity.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = rarityColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Item name
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Item type
                        Text(
                            text = when (item.type) {
                                "avatar" -> "Avatar"
                                "badge" -> "Badge"
                                "theme" -> "Theme"
                                "name_color" -> "Name Color"
                                else -> "Item"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Price section
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PurpleSurfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Current gold
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Gold:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Gold",
                                            tint = LegendaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = userGold.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LegendaryColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Divider(color = PurpleBackground)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Item price
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Price:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                    Row(
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

                                Spacer(modifier = Modifier.height(12.dp))

                                Divider(color = PurpleBackground)

                                Spacer(modifier = Modifier.height(12.dp))

                                // After purchase
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "After Purchase:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Gold",
                                            tint = if (canAfford) SuccessColor else ErrorColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (canAfford) (userGold - item.price).toString() else "---",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canAfford) SuccessColor else ErrorColor
                                        )
                                    }
                                }
                            }
                        }

                        if (!canAfford) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = ErrorColor.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = ErrorColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Not enough gold!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextSecondary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp
                                )
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Confirm button
                            Button(
                                onClick = onConfirm,
                                enabled = canAfford,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) SuccessColor else ErrorColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = if (canAfford) Icons.Default.ShoppingCart else Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (canAfford) "Purchase" else "Can't Buy",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to map asset URLs to drawable resources (same as in ShopItemCard)
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