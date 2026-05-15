package com.dersium.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dersium.core.ui.theme.DersiumColors

// ── Avatar ─────────────────────────────────────────────────────────────────────
@Composable
fun DersiumAvatar(
    initials: String,
    colorHex: String,
    size: Int = 48,
    modifier: Modifier = Modifier,
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrDefault(DersiumColors.Primary)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f))
            .border(1.5.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials.take(2).uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

// ── Status chip ────────────────────────────────────────────────────────────────
@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Primary FAB ────────────────────────────────────────────────────────────────
@Composable
fun DersiumFab(
    label: String,
    icon: ImageVector = Icons.Default.Add,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = DersiumColors.Primary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        icon = { Icon(icon, contentDescription = null) },
        text = { Text(label, fontWeight = FontWeight.SemiBold) },
    )
}

// ── Section header ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium, color = DersiumColors.Primary)
            }
        }
    }
}

// ── Dersium Text Field ─────────────────────────────────────────────────────────
@Composable
fun DersiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    placeholder: String = "",
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder, color = DersiumColors.TextTertiary) }) else null,
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = DersiumColors.TextSecondary) } },
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DersiumColors.Primary,
                unfocusedBorderColor = DersiumColors.Outline,
                focusedLabelColor = DersiumColors.Primary,
                unfocusedLabelColor = DersiumColors.TextSecondary,
                cursorColor = DersiumColors.Primary,
                focusedContainerColor = DersiumColors.SurfaceVariant,
                unfocusedContainerColor = DersiumColors.SurfaceVariant,
                errorBorderColor = DersiumColors.Expense,
                errorLabelColor = DersiumColors.Expense,
            ),
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = DersiumColors.Expense,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

// ── Empty State ────────────────────────────────────────────────────────────────
@Composable
fun DersiumEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(DersiumColors.SurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon, contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = DersiumColors.TextTertiary,
            )
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = DersiumColors.TextPrimary, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = DersiumColors.TextSecondary, textAlign = TextAlign.Center)
        action?.invoke()
    }
}

// ── Shimmer effect ─────────────────────────────────────────────────────────────
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing)),
        label = "shimmer",
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                DersiumColors.SurfaceVariant,
                DersiumColors.SurfaceElevated,
                DersiumColors.SurfaceVariant,
            ),
            start = Offset(translateAnim - 500f, 0f),
            end = Offset(translateAnim, 0f),
        )
    )
}

// ── Loading card shimmer ───────────────────────────────────────────────────────
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .shimmerEffect()
    )
}

// ── Summary stat card ──────────────────────────────────────────────────────────
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = DersiumColors.SurfaceVariant,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
        }
    }
}

// ── Currency formatter ─────────────────────────────────────────────────────────
fun Double.formatCurrency(currency: String = "₺"): String =
    "$currency${String.format("%,.0f", this)}"
