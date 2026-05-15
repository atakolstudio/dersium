package com.dersium.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dersium.core.ui.theme.DersiumColors

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val selectedColor: Color = DersiumColors.Primary,
)

val dersiumNavItems = listOf(
    BottomNavItem("home",      "Ana Sayfa",   Icons.Filled.Home,          Icons.Outlined.Home,            DersiumColors.Primary),
    BottomNavItem("students",  "Öğrenciler",  Icons.Filled.People,        Icons.Outlined.People,          DersiumColors.Primary),
    BottomNavItem("lessons",   "Dersler",     Icons.Filled.CalendarMonth,  Icons.Outlined.CalendarMonth,   DersiumColors.Primary),
    BottomNavItem("calendar",  "Takvim",      Icons.Filled.DateRange,      Icons.Outlined.DateRange,       DersiumColors.Primary),
    BottomNavItem("financial", "Finansal",    Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance,  DersiumColors.Income),
    BottomNavItem("reports",   "Rapor",       Icons.Filled.BarChart,       Icons.Outlined.BarChart,        DersiumColors.Primary),
)

@Composable
fun DersiumBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DersiumColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dersiumNavItems.forEach { item ->
                val selected = currentRoute == item.route
                BottomNavItemView(
                    item    = item,
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) item.selectedColor else DersiumColors.TextTertiary,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "iconColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "scale",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Pill background when selected
            if (selected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(item.selectedColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = item.selectedIcon,
                        contentDescription = item.label,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = item.unselectedIcon,
                    contentDescription = item.label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp).scale(scale),
                )
            }
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = iconColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
