package com.umbra.umbradex.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umbra.umbradex.R
import com.umbra.umbradex.data.model.User
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.calculateXpForLevel

@Composable
fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PurpleSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PurplePrimary.copy(alpha = 0.6f),
                                PurpleSecondary.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, PurpleTertiary, CircleShape)
                            .background(PurpleSurfaceVariant)
                    ) {
                        // Load avatar from drawable based on user's avatarUrl
                        Image(
                            painter = painterResource(id = getAvatarDrawableId(user.avatarUrl)),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Level
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Level",
                                tint = LegendaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Level ${user.level}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = LegendaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // XP Progress
                        val currentLevelXp = user.xp % 100
                        val nextLevelXp = 100
                        val progress = currentLevelXp.toFloat() / nextLevelXp

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "XP: $currentLevelXp / $nextLevelXp",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = if (progress.isNaN()) 0f else progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PurpleTertiary,
                                trackColor = PurpleSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Gold
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.money),
                                contentDescription = "Gold",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.gold.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LegendaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get avatar drawable ID
private fun getAvatarDrawableId(avatarUrl: String): Int {
    return when (avatarUrl) {
        "male1" -> R.drawable.standard_male1
        "male2" -> R.drawable.standard_male2
        "male3" -> R.drawable.standard_male3
        "male4" -> R.drawable.standard_male4
        "male5" -> R.drawable.standard_male5
        "female1" -> R.drawable.standard_female1
        "female2" -> R.drawable.standard_female2
        "female3" -> R.drawable.standard_female3
        "female4" -> R.drawable.standard_female4
        "female5" -> R.drawable.standard_female5
        else -> R.drawable.default_person
    }
}