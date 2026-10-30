package com.lucafilippi.rotaeuropa.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucafilippi.rotaeuropa.domain.ReserveProjection
import com.lucafilippi.rotaeuropa.domain.calculateReserveProjection
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.LocalDate
import com.lucafilippi.rotaeuropa.domain.TransactionKind
import com.lucafilippi.rotaeuropa.domain.budgetCategories
import com.lucafilippi.rotaeuropa.domain.calculateFinancialPlan
import com.lucafilippi.rotaeuropa.domain.convertBrlToEurMinor
import com.lucafilippi.rotaeuropa.domain.convertEurToBrlMinor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

@Composable
fun FinanceScreen(
    state: AppUiState,
    onSave: (String, String, String, String) -> Unit,
    onSaveTransaction: (String, TransactionKind, String, String) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onSaveBudget: (String, String, String, String, Boolean, String) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onSaveRate: (String, String, String, String) -> Unit,
    onSelectScenario: (Long) -> Unit,
    onCreateScenario: (String, String, String) -> Unit,
) {
    var balance by remember(state.reserve) { mutableStateOf(formatForInput(state.reserve.balanceMinor)) }
    var goal by remember(state.reserve) { mutableStateOf(formatForInput(state.reserve.goalMinor)) }
    var monthly by remember(state.reserve) { mutableStateOf(formatForInput(state.reserve.monthlyContributionMinor)) }
    var currency by remember(state.reserve) { mutableStateOf(state.reserve.currency) }
    var pendingCurrency by remember { mutableStateOf<String?>(null) }
    var transactionDialog by remember { mutableStateOf(false) }
    var budgetDialog by remember { mutableStateOf(false) }
    var rateDialog by remember { mutableStateOf(false) }
    var scenarioDialog by remember { mutableStateOf(false) }
    val projection = calculateReserveProjection(
        com.lucafilippi.rotaeuropa.domain.parseMoneyToMinorUnits(balance) ?: 0,
        com.lucafilippi.rotaeuropa.domain.parseMoneyToMinorUnits(goal) ?: 0,
        com.lucafilippi.rotaeuropa.domain.parseMoneyToMinorUnits(monthly) ?: 0,
    )

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("FINANÇAS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Sua reserva, com os pés no chão.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Defina valores e uma contribuição mensal para acompanhar o caminho até sua meta.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Cenários financeiros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Cada cenário guarda sua própria reserva, meta, custos e cotação.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = { scenarioDialog = true }) { Text("Novo") }
                }
                state.scenarios.forEach { scenario ->
                    FilterChip(selected = scenario.id == state.scenario.id, onClick = { onSelectScenario(scenario.id) }, label = { Text(scenario.name) })
                }
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Moeda de referência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EUR" to "Euro (€)", "BRL" to "Real (R$)").forEach { (code, label) ->
                        FilterChip(selected = currency == code, onClick = { if (currency != code) pendingCurrency = code }, label = { Text(label) })
                    }
                }
                Text("O app não consulta cotações. A equivalência só é calculada quando você cadastra uma cotação manual.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Projeção da reserva • próximos 6 meses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val balanceNow = state.reserve.balanceMinor.toFloat()
                val points = (0..6).map { month -> balanceNow + state.reserve.monthlyContributionMinor.toFloat() * month }
                ReserveProjectionChart(points)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hoje", style = MaterialTheme.typography.labelSmall)
                    Text("3 meses", style = MaterialTheme.typography.labelSmall)
                    Text("6 meses", style = MaterialTheme.typography.labelSmall)
                }
                Text("Estimativa sem rendimentos, inflação ou despesas futuras. Valores no cenário e moeda selecionados.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Histórico da reserva", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { transactionDialog = true }) { Text("Registrar") }
                }
                Text("Aportes e retiradas em $currency atualizam a reserva e ficam registrados no aparelho.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (state.extendedData.transactions.isEmpty()) Text("Nenhuma movimentação registrada.", style = MaterialTheme.typography.bodyMedium)
                state.extendedData.transactions.forEach { transaction ->
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${transaction.kind.label} • ${formatCurrency(transaction.amountMinor, transaction.currency)}", fontWeight = FontWeight.Medium)
                            Text("${LocalDate.ofEpochDay(transaction.occurredAtEpochDay).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}${transaction.note.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onDeleteTransaction(transaction.id) }) { Text("Desfazer") }
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Orçamento da mudança", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { budgetDialog = true }) { Text("Adicionar") }
                }
                Text("Os totais são separados por moeda para evitar somas sem cotação.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.extendedData.budget.groupBy { it.currency }.forEach { (code, entries) ->
                    val total = entries.sumOf { it.amountMinor }
                    Text("Total ${if (entries.any { it.recurring }) "previsto" else "único"} em $code: ${formatCurrency(total, code)}", fontWeight = FontWeight.SemiBold)
                    state.extendedData.exchangeRates.firstOrNull()?.let { rate ->
                        val equivalent = if (code == "BRL") convertBrlToEurMinor(total, rate.brlPerEurMinor)?.let { formatCurrency(it, "EUR") }
                        else convertEurToBrlMinor(total, rate.brlPerEurMinor)?.let { formatCurrency(it, "BRL") }
                        equivalent?.let { Text("Equivalência estimada na cotação cadastrada: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    entries.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Medium)
                                Text("${item.category}${if (item.recurring) " • recorrente" else " • único"} — ${formatCurrency(item.amountMinor, code)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { onDeleteBudget(item.id) }) { Text("Remover") }
                        }
                    }
                }
                if (state.extendedData.budget.isEmpty()) Text("Cadastre os custos previstos por categoria.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cotação manual BRL / EUR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { rateDialog = true }) { Text("Registrar") }
                }
                Text("Informe quantos reais equivalem a € 1. O app não busca cotações na internet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.extendedData.exchangeRates.firstOrNull()?.let { rate ->
                    Text("€ 1 = ${formatCurrency(rate.brlPerEurMinor, "BRL")} • ${LocalDate.ofEpochDay(rate.quotedAtEpochDay).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", fontWeight = FontWeight.SemiBold)
                    Text("Origem: ${rate.source.ifBlank { "não informada" }}${rate.note.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } ?: Text("Nenhuma cotação registrada.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quanto falta por mês até a data-alvo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val plan = calculateFinancialPlan(balanceMinor = state.reserve.balanceMinor, goalMinor = state.reserve.goalMinor, monthlyContributionMinor = state.reserve.monthlyContributionMinor, targetDateEpochDay = state.scenario.targetDateEpochDay, todayEpochDay = LocalDate.now().toEpochDay(), futureCostsMinor = state.extendedData.budget.filter { it.currency == currency && !it.recurring }.sumOf { it.amountMinor })
                if (plan == null) Text("Defina uma data-alvo no Início para calcular a necessidade mensal.", style = MaterialTheme.typography.bodyMedium)
                else {
                    Text("Aporte estimado necessário: ${formatCurrency(plan.amountNeededMonthlyMinor, currency)} por mês", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Projeção com o aporte atual: ${formatCurrency(plan.projectedBalanceMinor, currency)} em ${plan.monthsRemaining} meses. ${if (plan.feasible) "A meta está coberta nessa projeção." else "A projeção fica abaixo da meta."}", style = MaterialTheme.typography.bodyMedium)
                    Text("Inclui custos únicos cadastrados na mesma moeda. Não presume rendimentos nem soma custos em outra moeda.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Comparador rápido de metas em euro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                listOf(8_000L, 10_000L, 12_000L).forEach { euros ->
                    val target = euros * 100
                    val compared = calculateReserveProjection(state.reserve.balanceMinor, target, state.reserve.monthlyContributionMinor)
                    Text("€ ${euros} — faltam ${formatCurrency(compared.remainingMinor, "EUR")}${compared.monthsToGoal?.let { " • cerca de $it meses" }.orEmpty()}")
                }
                Text("Referências editáveis para comparação; não são recomendação nem cenário salvo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Reserva para a mudança", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                MoneyInput(balance, { balance = it }, "Reserva atual ($currency)")
                MoneyInput(goal, { goal = it }, "Meta total ($currency)")
                MoneyInput(monthly, { monthly = it }, "Aporte mensal planejado ($currency)")
                Text("Use valores positivos com até duas casas decimais. Ex.: 1250,00", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { onSave(balance, goal, monthly, currency) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar valores") }
            }
        }

        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Projeção da meta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (projection.remainingMinor == 0L && (com.lucafilippi.rotaeuropa.domain.parseMoneyToMinorUnits(goal) ?: 0L) > 0L) {
                    Text("Meta alcançada.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                } else {
                    Text("Faltam ${formatCurrency(projection.remainingMinor, currency)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { projection.percent / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Text("${projection.percent}% da meta", style = MaterialTheme.typography.bodyMedium)
                    Text(projectionDescription(projection, monthly, currency), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("O que esta projeção considera", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("A projeção usa a reserva atual e o aporte mensal planejado. Não inclui rendimentos, inflação, variação cambial ou despesas futuras.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    pendingCurrency?.let { newCurrency ->
        AlertDialog(
            onDismissRequest = { pendingCurrency = null },
            title = { Text("Alterar moeda?") },
            text = { Text("O app não fará conversão cambial. Os valores atuais serão associados a $newCurrency quando você salvar; informe os novos valores manualmente usando uma cotação que você escolher.") },
            confirmButton = { TextButton(onClick = { currency = newCurrency; pendingCurrency = null }) { Text("Entendi") } },
            dismissButton = { TextButton(onClick = { pendingCurrency = null }) { Text("Cancelar") } },
        )
    }
    if (transactionDialog) TransactionDialog(state.reserve.currency, onDismiss = { transactionDialog = false }, onSave = { amount, kind, note, date -> onSaveTransaction(amount, kind, note, date); transactionDialog = false })
    if (budgetDialog) BudgetDialog(state.reserve.currency, onDismiss = { budgetDialog = false }, onSave = { name, category, amount, code, recurring, note -> onSaveBudget(name, category, amount, code, recurring, note); budgetDialog = false })
    if (rateDialog) RateDialog(onDismiss = { rateDialog = false }, onSave = { rate, date, source, note -> onSaveRate(rate, date, source, note); rateDialog = false })
    if (scenarioDialog) ScenarioDialog(onDismiss = { scenarioDialog = false }, onSave = { name, destination, date -> onCreateScenario(name, destination, date); scenarioDialog = false })
}

@Composable
private fun ReserveProjectionChart(points: List<Float>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 8.dp, vertical = 8.dp)) {
        val maxValue = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val minValue = (points.minOrNull() ?: 0f).coerceAtMost(0f)
        val span = (maxValue - minValue).coerceAtLeast(1f)
        val coords = points.mapIndexed { index, value -> Offset(size.width * index / (points.lastIndex.coerceAtLeast(1)), size.height * (1f - (value - minValue) / span)) }
        for (row in 0..3) drawLine(gridColor, Offset(0f, size.height * row / 3f), Offset(size.width, size.height * row / 3f), strokeWidth = 1f)
        val path = Path().apply { coords.forEachIndexed { index, point -> if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y) } }
        drawPath(path, lineColor, style = Stroke(width = 5f))
        coords.forEach { drawCircle(lineColor, radius = 6f, center = it) }
    }
}

@Composable
private fun ScenarioDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Novo cenário financeiro") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nome") }, singleLine = true)
            OutlinedTextField(destination, { destination = it }, label = { Text("Destino (opcional)") }, singleLine = true)
            OutlinedTextField(date, { date = it }, label = { Text("Data-alvo (DD/MM/AAAA, opcional)") }, singleLine = true)
        }
    }, confirmButton = { TextButton(onClick = { onSave(name, destination, date) }) { Text("Criar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun TransactionDialog(currency: String, onDismiss: () -> Unit, onSave: (String, TransactionKind, String, String) -> Unit) {
    var amount by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
    var kind by remember { mutableStateOf(TransactionKind.CONTRIBUTION) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Movimentação da reserva") }, text = {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { TransactionKind.entries.forEach { FilterChip(selected = kind == it, onClick = { kind = it }, label = { Text(it.label) }) } }
            OutlinedTextField(amount, { amount = it }, label = { Text("Valor ($currency)") }, singleLine = true)
            OutlinedTextField(date, { date = it }, label = { Text("Data (DD/MM/AAAA)") }, singleLine = true)
            OutlinedTextField(note, { note = it }, label = { Text("Observação") }, minLines = 2)
        }
    }, confirmButton = { TextButton(onClick = { onSave(amount, kind, note, date) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun BudgetDialog(currencyDefault: String, onDismiss: () -> Unit, onSave: (String, String, String, String, Boolean, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var category by remember { mutableStateOf(budgetCategories.first()) }
    var amount by remember { mutableStateOf("") }; var currency by remember { mutableStateOf(currencyDefault) }
    var recurring by remember { mutableStateOf(false) }; var note by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Novo custo previsto") }, text = {
        Column(Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nome do custo") }, singleLine = true)
            Text("Categoria", style = MaterialTheme.typography.labelMedium)
            Column { budgetCategories.chunked(2).forEach { row -> Row { row.forEach { value -> FilterChip(selected = category == value, onClick = { category = value }, label = { Text(value) }, modifier = Modifier.padding(end = 4.dp)) } } } }
            OutlinedTextField(amount, { amount = it }, label = { Text("Valor") }, singleLine = true)
            Row { listOf("EUR", "BRL").forEach { code -> FilterChip(selected = currency == code, onClick = { currency = code }, label = { Text(code) }, modifier = Modifier.padding(end = 6.dp)) } }
            Row { Switch(checked = recurring, onCheckedChange = { recurring = it }); Text("Custo recorrente", modifier = Modifier.padding(top = 12.dp)) }
            OutlinedTextField(note, { note = it }, label = { Text("Observação") }, minLines = 2)
        }
    }, confirmButton = { TextButton(onClick = { onSave(name, category, amount, currency, recurring, note) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun RateDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var rate by remember { mutableStateOf("") }; var source by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Cotação manual") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(rate, { rate = it }, label = { Text("Reais por € 1 (R$)") }, singleLine = true)
            OutlinedTextField(date, { date = it }, label = { Text("Data (DD/MM/AAAA)") }, singleLine = true)
            OutlinedTextField(source, { source = it }, label = { Text("Origem da cotação") }, singleLine = true)
            OutlinedTextField(note, { note = it }, label = { Text("Observações") }, minLines = 2)
        }
    }, confirmButton = { TextButton(onClick = { onSave(rate, date, source, note) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun MoneyInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun SettingsScreen(state: AppUiState, onThemeChange: (Boolean) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("CONFIGURAÇÕES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Um espaço seu, no seu ritmo.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Ajuste a aparência e saiba onde seus dados ficam.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(shape = RoundedCornerShape(22.dp)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tema escuro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Escolha entre tema claro e escuro.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.darkTheme, onCheckedChange = onThemeChange)
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Seus dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Tarefas, cenário e valores financeiros são salvos localmente neste dispositivo. O app funciona sem conta e sem conexão com a internet.", style = MaterialTheme.typography.bodyMedium)
                Text("Use Mais → Backup e exportação para salvar uma cópia JSON ou CSV. A importação JSON pede confirmação antes de substituir os dados locais.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatForInput(minor: Long): String = java.math.BigDecimal.valueOf(minor, 2).toPlainString().replace('.', ',')

private fun formatCurrency(minor: Long, currency: String): String {
    val symbol = if (currency == "BRL") "R$" else "€"
    return "$symbol ${formatForInput(minor)}"
}

private fun projectionDescription(projection: ReserveProjection, monthly: String, currency: String): String {
    val monthlyMinor = com.lucafilippi.rotaeuropa.domain.parseMoneyToMinorUnits(monthly) ?: 0L
    return when {
        monthlyMinor <= 0L -> "Adicione um aporte mensal para estimar o prazo."
        projection.monthsToGoal == null -> "Não foi possível calcular a estimativa."
        else -> {
            val month = YearMonth.now().plusMonths(projection.monthsToGoal.toLong())
                .format(DateTimeFormatter.ofPattern("MMM 'de' yyyy", Locale("pt", "BR")))
            "Com ${formatCurrency(monthlyMinor, currency)} por mês, a meta pode ser alcançada em cerca de ${projection.monthsToGoal} meses (aprox. $month)."
        }
    }
}
