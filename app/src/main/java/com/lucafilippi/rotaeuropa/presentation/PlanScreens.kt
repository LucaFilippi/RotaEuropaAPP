package com.lucafilippi.rotaeuropa.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lucafilippi.rotaeuropa.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lucafilippi.rotaeuropa.domain.PlanTaskItem
import com.lucafilippi.rotaeuropa.domain.ProgressTask
import com.lucafilippi.rotaeuropa.domain.ReserveProjection
import com.lucafilippi.rotaeuropa.domain.TaskPriority
import com.lucafilippi.rotaeuropa.domain.TaskStatus
import com.lucafilippi.rotaeuropa.domain.calculateProgress
import com.lucafilippi.rotaeuropa.domain.calculateReserveProjection
import com.lucafilippi.rotaeuropa.domain.calculateReadiness
import java.text.NumberFormat
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

private val brazilianLocale = Locale("pt", "BR")
private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", brazilianLocale)

@Composable
fun OverviewScreen(
    state: AppUiState,
    onCompleteTask: (PlanTaskItem) -> Unit,
    onEditTargetDate: (String) -> Unit,
    onOpenPlan: () -> Unit,
) {
    var showTargetDateDialog by remember { mutableStateOf(false) }
    var targetDateText by remember(state.scenario.targetDateEpochDay) {
        mutableStateOf(state.scenario.targetDateEpochDay?.let { LocalDate.ofEpochDay(it).format(displayDateFormatter) }.orEmpty())
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Image(painterResource(R.drawable.rota_europa_logo), contentDescription = "Logo Rota Europa", contentScale = ContentScale.Crop, modifier = Modifier.size(76.dp).clip(RoundedCornerShape(18.dp)))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ROTA EUROPA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Seu plano, em movimento.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Uma etapa de cada vez, com clareza sobre o que falta.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.scenario.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                val targetDate = state.scenario.targetDateEpochDay?.let { LocalDate.ofEpochDay(it) }
                val targetLabel = targetDate?.format(displayDateFormatter) ?: "Data-alvo ainda não definida"
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(targetLabel, style = MaterialTheme.typography.bodyLarge)
                        if (targetDate != null) {
                            val remaining = targetDate.toEpochDay() - LocalDate.now().toEpochDay()
                            Text(
                                when {
                                    remaining > 0 -> "$remaining dias restantes"
                                    remaining == 0L -> "Data-alvo é hoje"
                                    else -> "Data-alvo passou há ${-remaining} dias"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    OutlinedButton(onClick = { showTargetDateDialog = true }) { Text(if (targetDate == null) "Definir" else "Editar") }
                }
            }
        }

        Card(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Progresso operacional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Ponderado pela importância das tarefas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${state.progress.percent}%", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(progress = { state.progress.percent / 100f }, modifier = Modifier.fillMaxWidth().height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniMetric("Concluídas", "${state.progress.completed}/${state.progress.total}", Modifier.weight(1f))
                    MiniMetric("Atrasadas", state.progress.overdue.toString(), Modifier.weight(1f))
                    MiniMetric("Obrigatórias", state.progress.pendingRequired.toString(), Modifier.weight(1f))
                }
                if (state.progress.pendingRequired > 0) {
                    Text("Ainda há requisitos obrigatórios pendentes. O percentual não indica prontidão para a mudança.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        val requiredDocuments = state.extendedData.journeyRecords.filter { record ->
            record.recordType == "document" && record.details.lineSequence().any { line -> line.startsWith("Obrigatório para qual etapa?:") && line.substringAfter(':').isNotBlank() }
        }
        val readiness = calculateReadiness(
            destinationConfigured = state.scenario.destination.isNotBlank(),
            targetDateConfigured = state.scenario.targetDateEpochDay?.let { it > LocalDate.now().toEpochDay() } == true,
            reserveGoalConfigured = state.reserve.goalMinor > 0,
            reserveGoalReached = state.reserve.goalMinor > 0 && state.reserve.balanceMinor >= state.reserve.goalMinor,
            requiredTasksComplete = state.progress.pendingRequired == 0,
            documentsConfigured = requiredDocuments.isNotEmpty(),
            requiredDocumentsComplete = requiredDocuments.isNotEmpty() && requiredDocuments.all { it.status == "Concluído" },
        )
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Indicador de prontidão", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${readiness.score}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(progress = { readiness.score / 100f }, modifier = Modifier.fillMaxWidth())
                Text("Checklist orientativo por cenário; não substitui validação oficial de visto, documentos ou orçamento.", style = MaterialTheme.typography.bodySmall)
                readiness.blockers.take(3).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }

        FinanceSummaryCard(state)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Próximas ações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onOpenPlan) { Text("Ver plano") }
            }
            val next = state.tasks
                .filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
                .sortedWith(compareByDescending<PlanTaskItem> { it.priority.order }.thenBy { it.dueDateEpochDay ?: Long.MAX_VALUE }.thenByDescending { it.importance })
                .take(5)
            if (next.isEmpty()) {
                EmptyMessage("Seu plano começa com uma ação", "Cadastre a primeira tarefa e acompanhe seu avanço por aqui.", onOpenPlan, "Criar primeira tarefa")
            } else {
                next.forEach { TaskSummaryRow(it, onCompleteTask) }
            }
        }
        Spacer(Modifier.height(10.dp))
    }

    if (showTargetDateDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDateDialog = false },
            title = { Text("Data-alvo do cenário") },
            text = {
                OutlinedTextField(
                    value = targetDateText,
                    onValueChange = { targetDateText = it },
                    label = { Text("Data (DD/MM/AAAA)") },
                    placeholder = { Text("Ex.: 15/09/2028") },
                    singleLine = true,
                    supportingText = { Text("Deixe em branco se ainda não houver uma data definida.") },
                )
            },
            confirmButton = {
                TextButton(onClick = { onEditTargetDate(targetDateText); showTargetDateDialog = false }) { Text("Salvar") }
            },
            dismissButton = {
                Row {
                    if (state.scenario.targetDateEpochDay != null) TextButton(onClick = { targetDateText = ""; onEditTargetDate(""); showTargetDateDialog = false }) { Text("Remover") }
                    TextButton(onClick = { showTargetDateDialog = false }) { Text("Cancelar") }
                }
            },
        )
    }
}

@Composable
private fun FinanceSummaryCard(state: AppUiState) {
    val reserve = state.reserve
    val projection = calculateReserveProjection(reserve.balanceMinor, reserve.goalMinor, reserve.monthlyContributionMinor)
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Reserva para a mudança", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (reserve.goalMinor == 0L) {
                Text("Defina sua meta financeira para acompanhar quanto falta.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("${formatMoney(reserve.balanceMinor, reserve.currency)} reservados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Meta: ${formatMoney(reserve.goalMinor, reserve.currency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${projection.percent}%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(progress = { projection.percent / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
                Text("Faltam ${formatMoney(projection.remainingMinor, reserve.currency)}", style = MaterialTheme.typography.bodyMedium)
                Text(projectionLabel(projection, reserve.monthlyContributionMinor, reserve.currency), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun projectionLabel(projection: ReserveProjection, monthlyMinor: Long, currency: String): String = when {
    projection.remainingMinor == 0L -> "Meta alcançada."
    monthlyMinor <= 0L -> "Informe um aporte mensal para ver a projeção."
    projection.monthsToGoal == null -> "Não foi possível estimar a projeção."
    else -> {
        val month = YearMonth.now().plusMonths(projection.monthsToGoal.toLong()).format(DateTimeFormatter.ofPattern("MMM 'de' yyyy", brazilianLocale))
        "Com ${formatMoney(monthlyMinor, currency)} por mês: cerca de ${projection.monthsToGoal} meses (até $month)."
    }
}

@Composable
fun PlanScreen(
    state: AppUiState,
    onSaveTask: (Long, Long, String, String, String, TaskPriority, TaskStatus, String, Int, Boolean) -> Boolean,
    onDeleteTask: (Long) -> Unit,
    onCompleteTask: (PlanTaskItem) -> Unit,
) {
    var editorTask by remember { mutableStateOf<PlanTaskItem?>(null) }
    var createNew by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(TaskFilter.ACTIVE) }
    var priorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var pendingDelete by remember { mutableStateOf<PlanTaskItem?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Plano de saída", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Organize as tarefas por fase e prioridade.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { editorTask = null; createNew = true }) { Text("+ Tarefa") }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TaskFilter.entries.forEach { item ->
                FilterChip(selected = filter == item, onClick = { filter = item }, label = { Text(item.label) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            FilterChip(selected = priorityFilter == null, onClick = { priorityFilter = null }, label = { Text("Qualquer prioridade") })
            TaskPriority.entries.forEach { item ->
                FilterChip(selected = priorityFilter == item, onClick = { priorityFilter = if (priorityFilter == item) null else item }, label = { Text(item.label) })
            }
        }
        Spacer(Modifier.height(6.dp))
        if (state.phases.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val visibleTasks = state.tasks.filter {
                when (filter) {
                    TaskFilter.ACTIVE -> it.status == TaskStatus.NOT_STARTED || it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.BLOCKED
                    TaskFilter.IN_PROGRESS -> it.status == TaskStatus.IN_PROGRESS
                    TaskFilter.BLOCKED -> it.status == TaskStatus.BLOCKED
                    TaskFilter.COMPLETED -> it.status == TaskStatus.COMPLETED
                    TaskFilter.ALL -> true
                } && (priorityFilter == null || it.priority == priorityFilter)
            }
            if (visibleTasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyMessage(
                        title = when (filter) {
                            TaskFilter.COMPLETED -> "Ainda não há tarefas concluídas"
                            TaskFilter.ACTIVE -> "Tudo começa com uma tarefa"
                            TaskFilter.IN_PROGRESS -> "Nenhuma tarefa em andamento"
                            TaskFilter.BLOCKED -> "Nenhuma tarefa bloqueada"
                            TaskFilter.ALL -> "Seu plano está vazio"
                        },
                        body = "Adicione uma ação concreta para começar a transformar o plano em etapas.",
                        onAction = { editorTask = null; createNew = true },
                        actionLabel = "Adicionar tarefa",
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    state.phases.forEach { phase ->
                        val phaseTasks = visibleTasks.filter { it.phaseId == phase.id }
                            .sortedWith(compareByDescending<PlanTaskItem> { it.priority.order }.thenBy { it.dueDateEpochDay ?: Long.MAX_VALUE })
                        if (phaseTasks.isNotEmpty()) {
                            item(key = "phase-${phase.id}") {
                                Text(phase.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
                            }
                            items(phaseTasks, key = { it.id }) { task ->
                                PlanTaskCard(task, onComplete = { onCompleteTask(task) }, onEdit = { editorTask = task; createNew = false }, onDelete = { pendingDelete = task })
                            }
                        }
                    }
                }
            }
        }
    }

    if (createNew || editorTask != null) {
        TaskEditorDialog(
            task = editorTask,
            phases = state.phases,
            onDismiss = { createNew = false; editorTask = null },
            onSave = { id, phaseId, title, description, category, priority, status, due, importance, required ->
                if (onSaveTask(id, phaseId, title, description, category, priority, status, due, importance, required)) {
                    createNew = false
                    editorTask = null
                    true
                } else {
                    false
                }
            },
        )
    }
    pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir tarefa?") },
            text = { Text("“${task.title}” será removida do plano.") },
            confirmButton = { TextButton(onClick = { onDeleteTask(task.id); pendingDelete = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } },
        )
    }
}

private enum class TaskFilter(val label: String) { ACTIVE("Em aberto"), IN_PROGRESS("Em andamento"), BLOCKED("Bloqueadas"), COMPLETED("Concluídas"), ALL("Todas") }

@Composable
private fun PlanTaskCard(task: PlanTaskItem, onComplete: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.status == TaskStatus.COMPLETED, onCheckedChange = { onComplete() })
            Column(Modifier.weight(1f).clickable(onClick = onEdit).padding(horizontal = 4.dp)) {
                Text(task.title, fontWeight = FontWeight.SemiBold, textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(task.priority.label, style = MaterialTheme.typography.labelSmall, color = priorityColor(task.priority))
                    Text(task.status.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    task.dueDateEpochDay?.let { Text(LocalDate.ofEpochDay(it).format(displayDateFormatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                if (task.required) Text("Requisito obrigatório", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            TextButton(onClick = onEdit) { Text("Editar") }
            TextButton(onClick = onDelete) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    task: PlanTaskItem?,
    phases: List<com.lucafilippi.rotaeuropa.domain.PlanPhaseItem>,
    onDismiss: () -> Unit,
    onSave: (Long, Long, String, String, String, TaskPriority, TaskStatus, String, Int, Boolean) -> Boolean,
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var description by remember(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var category by remember(task?.id) { mutableStateOf(task?.category ?: "Geral") }
    var phaseId by remember(task?.id, phases) { mutableStateOf(task?.phaseId ?: phases.firstOrNull()?.id ?: 0L) }
    var priority by remember(task?.id) { mutableStateOf(task?.priority ?: TaskPriority.NORMAL) }
    var status by remember(task?.id) { mutableStateOf(task?.status ?: TaskStatus.NOT_STARTED) }
    var dueDate by remember(task?.id) { mutableStateOf(task?.dueDateEpochDay?.let { LocalDate.ofEpochDay(it).format(displayDateFormatter) }.orEmpty()) }
    var importance by remember(task?.id) { mutableStateOf(task?.importance ?: 3) }
    var required by remember(task?.id) { mutableStateOf(task?.required ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "Nova tarefa" else "Editar tarefa") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Título *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Descrição") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Categoria") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                MenuChoice("Fase", phases.firstOrNull { it.id == phaseId }?.name ?: "Selecione", phases.map { it.id to it.name }) { phaseId = it }
                MenuChoice("Prioridade", priority.label, TaskPriority.entries.map { it to it.label }) { priority = it }
                MenuChoice("Status", status.label, TaskStatus.entries.map { it to it.label }) { status = it }
                OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Prazo (DD/MM/AAAA)") }, placeholder = { Text("Opcional") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Importância estratégica", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { value -> FilterChip(selected = importance == value, onClick = { importance = value }, label = { Text(value.toString()) }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = required, onCheckedChange = { required = it })
                    Column {
                        Text("Requisito obrigatório", style = MaterialTheme.typography.bodyMedium)
                        Text("Pendência essencial para a mudança", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(task?.id ?: 0L, phaseId, title, description, category, priority, status, dueDate, importance, required) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun <T> MenuChoice(label: String, selected: String, choices: List<Pair<T, String>>, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(selected); Text("⌄") }
            }
            androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                choices.forEach { (value, name) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { onSelect(value); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskSummaryRow(task: PlanTaskItem, onComplete: (PlanTaskItem) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = false, onCheckedChange = { onComplete(task) })
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(task.priority.label, task.dueDateEpochDay?.let { LocalDate.ofEpochDay(it).format(displayDateFormatter) }).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = priorityColor(task.priority),
                )
            }
            if (task.required) Text("Obrigatória", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyMessage(title: String, body: String, onAction: () -> Unit, actionLabel: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("○", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

private fun formatMoney(minorUnits: Long, currencyCode: String): String {
    val formatter = NumberFormat.getCurrencyInstance(brazilianLocale)
    formatter.currency = runCatching { Currency.getInstance(currencyCode) }.getOrDefault(Currency.getInstance("EUR"))
    return formatter.format(BigDecimal.valueOf(minorUnits, 2))
}

@Composable
private fun priorityColor(priority: TaskPriority) = when (priority) {
    TaskPriority.CRITICAL -> MaterialTheme.colorScheme.error
    TaskPriority.HIGH -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
