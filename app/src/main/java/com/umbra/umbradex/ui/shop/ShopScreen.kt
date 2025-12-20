package com.umbra.umbradex.ui.shop

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.ui.components.LoadingOverlay
import com.umbra.umbradex.ui.shop.components.PurchaseConfirmDialog
import com.umbra.umbradex.ui.shop.components.ShopItemCard
import com.umbra.umbradex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen() {
    val viewModel = remember {
        ShopViewModel(
            authRepository = AuthRepository(),
            shopRepository = ShopRepository(),
            inventoryRepository = InventoryRepository(),
            userRepository = UserRepository()
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
        }
    }

    // Purchase confirmation dialog
    if (uiState.showPurchaseDialog && uiState.selectedItem != null) {
        PurchaseConfirmDialog(
            item = uiState.selectedItem!!,
            userGold = uiState.userGold,
            onConfirm = {
                viewModel.confirmPurchase()
            },
            onDismiss = {
                viewModel.dismissPurchaseDialog()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PurpleBackground,
                        PurpleSurface
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Shop",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleTertiary
                    )
                },
                actions = {
                    // Gold display
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = LegendaryColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                                text = uiState.userGold.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LegendaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurpleBackground
                )
            )

            // Content
            if (uiState.isLoading && uiState.shopItems.isEmpty()) {
                LoadingOverlay(message = "Loading shop items...")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Category tabs
                    ScrollableTabRow(
                        selectedTabIndex = uiState.selectedCategoryIndex,
                        containerColor = Color.Transparent,
                        contentColor = PurpleTertiary,
                        edgePadding = 0.dp
                    ) {
                        ShopCategory.entries.forEachIndexed { index, category ->
                            Tab(
                                selected = uiState.selectedCategoryIndex == index,
                                onClick = { viewModel.selectCategory(category) },
                                text = {
                                    Text(
                                        text = category.displayName,
                                        fontWeight = if (uiState.selectedCategoryIndex == index)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = category.displayName
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rarity filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RarityFilter.entries.forEach { rarity ->
                            FilterChip(
                                selected = uiState.selectedRarity == rarity,
                                onClick = { viewModel.selectRarity(rarity) },
                                label = {
                                    Text(
                                        text = rarity.displayName,
                                        fontWeight = if (uiState.selectedRarity == rarity)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(rarity.color),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Items count
                    Text(
                        text = "${uiState.displayedItems.size} items available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shop items list
                    if (uiState.displayedItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🛍️",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Items Found",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try a different category or filter",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = uiState.displayedItems,
                                key = { it.id }
                            ) { item ->
                                val isOwned = uiState.ownedItemIds.contains(item.id)
                                val canAfford = uiState.userGold >= item.price

                                ShopItemCard(
                                    item = item,
                                    isOwned = isOwned,
                                    canAfford = canAfford,
                                    onClick = {
                                        if (!isOwned && canAfford) {
                                            viewModel.showPurchaseDialog(item)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

enum class ShopCategory(
    val displayName: String,
    val type: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL("All", "all", Icons.Default.ShoppingCart),
    THEMES("Themes", "theme", Icons.Default.Palette),
    AVATARS("Avatars", "avatar", Icons.Default.Person),
    BADGES("Badges", "badge", Icons.Default.Star),
    NAME_COLORS("Name Colors", "name_color", Icons.Default.ColorLens)
}

enum class RarityFilter(
    val displayName: String,
    val rarity: String?,
    val color: Long
) {
    ALL("All", null, 0xFF9E9E9E),
    COMMON("Common", "common", 0xFF9E9E9E),
    RARE("Rare", "rare", 0xFF2196F3),
    EPIC("Epic", "epic", 0xFF9C27B0),
    LEGENDARY("Legendary", "legendary", 0xFFFFD700)
}