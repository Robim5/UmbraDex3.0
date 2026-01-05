package com.umbra.umbradex.ui.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.model.UserProfile
import com.umbra.umbradex.ui.components.UmbraBottomNav
import com.umbra.umbradex.utils.getAvatarResourceId
import com.umbra.umbradex.utils.toBrush
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.umbra.umbradex.utils.RarityUtils

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
    userId: String
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadInventory(userId)
    }

    // Filtrar itens por categoria selecionada
    val filteredItems = remember(uiState.inventoryItems, uiState.selectedCategory) {
        uiState.inventoryItems.filter { it.item.type == uiState.selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            InventoryHeader()

            // Preview do equipamento atual
            EquippedItemsPreview(
                equippedItems = uiState.equippedItems
            )

            // Filtros de categoria
            CategoryFilters(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // Grid de itens
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items in this category yet!",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems) { inventoryItem ->
                        InventoryItemCard(
                            item = inventoryItem.item,
                            isEquipped = isItemEquipped(inventoryItem.item, uiState.equippedItems),
                            onClick = {
                                viewModel.equipItem(userId, inventoryItem.item.name, inventoryItem.item.type)
                            }
                        )
                    }
                }
            }
        }

        // Snackbars
        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearMessages()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Text(message, color = Color.White)
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearMessages()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFFF44336)
            ) {
                Text(error, color = Color.White)
            }
        }
    }
}

@Composable
fun InventoryHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🎒 Inventory",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EquippedItemsPreview(equippedItems: EquippedItems) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Currently Equipped",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Skin
                EquippedItemSlot(
                    label = "Skin",
                    item = equippedItems.skin,
                    icon = "👤"
                )

                // Badge
                EquippedItemSlot(
                    label = "Badge",
                    item = equippedItems.badge,
                    icon = "🏆"
                )

                // Theme
                EquippedItemSlot(
                    label = "Theme",
                    item = equippedItems.theme,
                    icon = "🎨"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            // Title e Partner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📜 Title",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = equippedItems.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🐾 Partner",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (equippedItems.partnerPokemonId != null)
                            "#${equippedItems.partnerPokemonId}"
                        else "None",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EquippedItemSlot(
    label: String,
    item: ShopItem?,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$icon $label",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (item?.type == "theme" && item.colors != null) {
                        Brush.linearGradient(
                            item.colors.mapNotNull {
                                try {
                                    Color(android.graphics.Color.parseColor(it))
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        )
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF424242), Color(0xFF616161)))
                    }
                )
                .border(
                    2.dp,
                    if (item != null) RarityUtils.getColor(item.rarity) else Color(0xFF616161),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item != null && item.type != "theme" && item.assetUrl != null) {
                val resourceId = getAvatarResourceId(item.assetUrl)
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = item.name,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Text(
            text = item?.name ?: "None",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun CategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "👤 Skins" to "skin",
        "🎨 Themes" to "theme",
        "🏆 Badges" to "badge",
        "🌈 Colors" to "name_color"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (label, category) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    item: ShopItem,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    val rarityColor = RarityUtils.getColor(item.rarity)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF1B5E20).copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge de raridade e equipped
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = rarityColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, rarityColor)
                    ) {
                        Text(
                            text = item.rarity.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = rarityColor
                        )
                    }

                    if (isEquipped) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Equipped",
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Imagem do item
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (item.type == "theme" && item.colors != null) {
                                Brush.linearGradient(
                                    item.colors.mapNotNull {
                                        try {
                                            Color(android.graphics.Color.parseColor(it))
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                )
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF424242), Color(0xFF616161)))
                            }
                        )
                        .border(2.dp, rarityColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.type != "theme" && item.assetUrl != null) {
                        val resourceId = getAvatarResourceId(item.assetUrl)
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = item.name,
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nome
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // Status
                if (isEquipped) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Text(
                            text = "EQUIPPED",
                            modifier = Modifier.padding(vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Equip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper para verificar se item está equipado
fun isItemEquipped(item: ShopItem, equipped: EquippedItems): Boolean {
    return when (item.type) {
        "skin" -> item.name == equipped.skin?.name
        "theme" -> item.name == equipped.theme?.name
        "badge" -> item.name == equipped.badge?.name
        "name_color" -> item.name == equipped.nameColor?.name
        else -> false
    }
}