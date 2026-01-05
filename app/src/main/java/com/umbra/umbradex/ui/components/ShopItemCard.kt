package com.umbra.umbradex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import com.umbra.umbradex.data.model.ShopItem
import com.umbra.umbradex.ui.theme.UmbraGold
import com.umbra.umbradex.ui.theme.UmbraPrimary
import com.umbra.umbradex.ui.theme.UmbraSurface
import com.umbra.umbradex.utils.RarityUtils
import com.umbra.umbradex.utils.getAvatarResourceId
import com.umbra.umbradex.utils.toBrush
import com.umbra.umbradex.utils.toColor

@Composable
fun ShopItemCard(
    item: ShopItem,
    userGold: Long,
    userLevel: Int,
    onBuyClick: () -> Unit
) {
    val rarityColor = RarityUtils.getColor(item.rarity)

    // Regras do PDF: Bloqueio por Nível
    val isLevelLocked = userLevel < item.minLevel
    val canAfford = userGold >= item.price

    // Design do Cartão
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .border(1.dp, rarityColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UmbraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. VISUAL PREVIEW (O Recheio) ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                when (item.type) {
                    "theme" -> ThemePreview(item.colors)
                    "name_color" -> NameColorPreview(item.colors)
                    else -> AssetPreview(item.assetUrl ?: "") // Skins e Badges
                }

                // CADEADO (Overlay) se bloqueado
                if (isLevelLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White)
                            Text("Lvl ${item.minLevel}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 2. DETALHES ---
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                fontWeight = FontWeight.Bold
            )

            // Raridade (Badge Pequeno)
            Surface(
                color = rarityColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = item.rarity.uppercase(),
                    color = rarityColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 3. BOTÃO DE COMPRA ---
            Button(
                onClick = onBuyClick,
                enabled = !isLevelLocked && canAfford,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) UmbraGold else Color.Red.copy(alpha = 0.6f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isLevelLocked) {
                    Text("LOCKED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else if (!canAfford) {
                    Text("${item.price} G", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${item.price} G", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTES DE PREVIEW ---

@Composable
fun AssetPreview(assetName: String) {
    val resId = getAvatarResourceId(assetName)
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        )
    } else {
        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun ThemePreview(colors: List<String>?) {
    // Mostra 3 bolinhas com as cores principais do tema
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val palette = colors ?: listOf("#FFFFFF", "#000000", "#888888")
        palette.take(3).forEachIndexed { index, hex ->
            Box(
                modifier = Modifier
                    .size(if (index == 1) 32.dp else 24.dp) // A do meio é maior
                    .clip(CircleShape)
                    .background(hex.toColor())
                    .border(1.dp, Color.White, CircleShape)
            )
            if (index < 2) Spacer(modifier = Modifier.width((-8).dp)) // Sobreposição estilo Apple
        }
    }
}

@Composable
fun NameColorPreview(colors: List<String>?) {
    // Mostra o texto com Gradiente
    val brush = (colors ?: listOf("#FFFFFF")).toBrush()
    Text(
        text = "Name",
        style = MaterialTheme.typography.titleLarge.copy(
            brush = brush
        ),
        fontWeight = FontWeight.ExtraBold
    )
}