package com.dersium.feature.financial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.domain.model.Expense
import com.dersium.core.domain.model.ExtraIncome
import com.dersium.core.ui.components.*
import com.dersium.core.ui.theme.DersiumColors
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinancialScreen(viewModel: FinancialViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(DersiumColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            // Tab toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TabButton(
                    label = "Ek Gelirler",
                    icon = Icons.Default.TrendingUp,
                    selected = state.tab == FinancialTab.INCOME,
                    selectedColor = DersiumColors.Income,
                    onClick = { viewModel.setTab(FinancialTab.INCOME) },
                    modifier = Modifier.weight(1f),
                )
                TabButton(
                    label = "Giderler",
                    icon = Icons.Default.TrendingDown,
                    selected = state.tab == FinancialTab.EXPENSE,
                    selectedColor = DersiumColors.Expense,
                    onClick = { viewModel.setTab(FinancialTab.EXPENSE) },
                    modifier = Modifier.weight(1f),
                )
            }

            val isIncome = state.tab == FinancialTab.INCOME
            val total = if (isIncome) state.totalExtraIncome else state.totalExpense
            val color = if (isIncome) DersiumColors.Income else DersiumColors.Expense
            val bgColor = if (isIncome) DersiumColors.IncomeContainer else DersiumColors.ExpenseContainer
            val title = if (isIncome) "Ek Gelirler" else "Giderler"
            val icon = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown

            // Summary card
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = bgColor,
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Toplam ${title}", style = MaterialTheme.typography.bodyMedium, color = color.copy(alpha = 0.8f))
                        Text(total.formatCurrency(state.currency), style = MaterialTheme.typography.headlineMedium, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
                        val count = if (isIncome) state.extraIncomes.size else state.expenses.size
                        Text("$count kayıt", style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
                    }
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                    }
                }
            }

            // List
            val items = if (isIncome) state.extraIncomes else state.expenses
            if (items.isEmpty()) {
                DersiumEmptyState(
                    icon = if (isIncome) Icons.Outlined.AttachMoney else Icons.Outlined.ShoppingCart,
                    title = "Henüz ${title.lowercase()} yok",
                    subtitle = "Sağ alttaki butona basarak ekleyin",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp, ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items, key = {
                        when (it) {
                            is ExtraIncome -> "income_${it.id}"
                            is Expense -> "expense_${it.id}"
                            else -> it.hashCode()
                        }
                    }) { item ->
                        FinancialItemCard(
                            item = item,
                            currency = state.currency,
                            color = color,
                            onDelete = {
                                when (item) {
                                    is ExtraIncome -> viewModel.deleteIncome(item)
                                    is Expense -> viewModel.deleteExpense(item)
                                }
                            },
                        )
                    }
                }
            }
        }

        DersiumFab(
            label = if (state.tab == FinancialTab.INCOME) "Gelir Ekle" else "Gider Ekle",
            onClick = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showAddSheet) {
        AddFinancialBottomSheet(
            isIncome = state.tab == FinancialTab.INCOME,
            onDismiss = { showAddSheet = false },
            onSave = { title, amount, notes ->
                if (state.tab == FinancialTab.INCOME) viewModel.addIncome(title, amount, notes)
                else viewModel.addExpense(title, amount, notes)
                showAddSheet = false
            },
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) selectedColor else DersiumColors.SurfaceVariant,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = if (selected) Color.White else DersiumColors.TextSecondary, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, color = if (selected) Color.White else DersiumColors.TextSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FinancialItemCard(item: Any, currency: String, color: Color, onDelete: () -> Unit) {
    val title = when (item) { is ExtraIncome -> item.title; is Expense -> item.title; else -> "" }
    val amount = when (item) { is ExtraIncome -> item.amount; is Expense -> item.amount; else -> 0.0 }
    val date = when (item) {
        is ExtraIncome -> item.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr")))
        is Expense -> item.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr")))
        else -> ""
    }
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DersiumColors.SurfaceVariant) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
                    Icon(Icons.Default.AttachMoney, null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = DersiumColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(date, style = MaterialTheme.typography.bodySmall, color = DersiumColors.TextSecondary)
            }
            Text(amount.formatCurrency(currency), style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = DersiumColors.Expense, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFinancialBottomSheet(isIncome: Boolean, onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DersiumColors.Surface) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (isIncome) "Gelir Ekle" else "Gider Ekle", style = MaterialTheme.typography.titleLarge, color = DersiumColors.TextPrimary, fontWeight = FontWeight.Bold)
            DersiumTextField(value = title, onValueChange = { title = it }, label = "Başlık *", leadingIcon = Icons.Default.Label)
            DersiumTextField(value = amount, onValueChange = { amount = it }, label = "Tutar (₺) *", leadingIcon = Icons.Default.AttachMoney, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            DersiumTextField(value = notes, onValueChange = { notes = it }, label = "Notlar", leadingIcon = Icons.Default.Notes)
            Button(
                onClick = { amount.toDoubleOrNull()?.let { if (title.isNotBlank()) onSave(title, it, notes) } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) DersiumColors.Income else DersiumColors.Expense),
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
