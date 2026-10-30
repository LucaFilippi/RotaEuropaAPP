package com.lucafilippi.rotaeuropa.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucafilippi.rotaeuropa.data.local.JourneyRecordEntity
import com.lucafilippi.rotaeuropa.domain.journeyRecordTypes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.content.ContextCompat
import com.lucafilippi.rotaeuropa.LocalReminderNotifications

private val moreDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun MoreScreen(
    state: AppUiState,
    onSaveRecord: (String, String, String, String, String) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onChangeRecordStatus: (Long, String) -> Unit,
    onSaveReview: (String, String, String) -> Unit,
    onOpenBackup: () -> Unit,
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showReview by remember { mutableStateOf(false) }
    var showAddRecord by remember { mutableStateOf(false) }
    var pendingReminder by remember { mutableStateOf<PendingReminder?>(null) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingReminder?.let { reminder ->
            onSaveRecord(reminder.type, reminder.title, reminder.details, reminder.status, reminder.dueDate)
            if (granted) reminder.epochDay?.let { LocalReminderNotifications.schedule(context, reminder.title, it) }
            pendingReminder = null
            showAddRecord = false
        }
    }
    val selectedLabel = journeyRecordTypes.firstOrNull { it.second == selectedType }?.first
    val records = state.extendedData.journeyRecords.filter { it.recordType == selectedType }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("PLANEJAMENTO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(selectedLabel ?: "Mais áreas do seu plano", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (selectedType == null && !showReview) {
            Text("Cadastros locais para carreira, estudo, idiomas, documentos e destinos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Relatório de acompanhamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Tarefas concluídas: ${state.progress.percent}% • ${state.extendedData.quarterlyReviews.size} revisão(ões) trimestral(is)", style = MaterialTheme.typography.bodyMedium)
                    state.extendedData.quarterlyReviews.take(4).forEach { review ->
                        Text("${LocalDate.ofEpochDay(review.reviewedAtEpochDay).format(moreDateFormat)} • risco: ${review.risk.ifBlank { "não registrado" }} • prioridades: ${review.priorities}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            val reminders = state.extendedData.journeyRecords.filter { it.recordType == "reminder" }.sortedBy { it.dueDateEpochDay }
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Lembretes locais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Os lembretes ficam salvos neste aparelho e aparecem aqui com seu prazo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    reminders.take(5).forEach { reminder -> Text("${reminder.dueDateEpochDay?.let { LocalDate.ofEpochDay(it).format(moreDateFormat) } ?: "Sem data"} • ${reminder.title}") }
                    if (reminders.isEmpty()) Text("Nenhum lembrete cadastrado.", style = MaterialTheme.typography.bodySmall)
                }
            }
            journeyRecordTypes.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { (label, type) ->
                        Card(onClick = { selectedType = type }, modifier = Modifier.weight(1f)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${state.extendedData.journeyRecords.count { it.recordType == type }} registro(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
            Card(onClick = { showReview = true }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Revisões trimestrais", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("${state.extendedData.quarterlyReviews.size} revisão(ões) salva(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(onClick = onOpenBackup) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Backup e exportação", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Salvar uma cópia local dos dados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (selectedType != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { selectedType = null }) { Text("Voltar") }
                Button(onClick = { showAddRecord = true }) { Text("Adicionar") }
            }
            if (records.isEmpty()) Text("Nenhum registro ainda. Use Adicionar para começar.")
            if (selectedType == "project") {
                listOf("Backlog", "Planejado", "Em andamento", "Revisão", "Concluído", "Outros").forEach { column ->
                    Text(column, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    records.filter { if (column == "Outros") it.status !in listOf("Backlog", "Planejado", "Em andamento", "Revisão", "Concluído") else it.status == column }.forEach { item ->
                        RecordCard(item, onDelete = { onDeleteRecord(item.id) }, onChangeStatus = { onChangeRecordStatus(item.id, nextProjectStatus(item.status)) })
                    }
                }
            } else {
                records.forEach { item ->
                    RecordCard(item, onDelete = {
                        if (item.recordType == "reminder") LocalReminderNotifications.cancel(context, item.title)
                        onDeleteRecord(item.id)
                    }, onChangeStatus = if (selectedType == "document") ({ onChangeRecordStatus(item.id, if (item.status == "Concluído") "Pendente" else "Concluído") }) else null)
                }
            }
        }

        if (showReview) {
            OutlinedButton(onClick = { showReview = false }) { Text("Voltar") }
            QuarterlyReviewForm(onSave = { answers, priorities, risk -> onSaveReview(answers, priorities, risk); showReview = false })
            state.extendedData.quarterlyReviews.forEach { review ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Revisão de ${LocalDate.ofEpochDay(review.reviewedAtEpochDay).format(moreDateFormat)}", fontWeight = FontWeight.SemiBold)
                        Text("Risco: ${review.risk.ifBlank { "não informado" }}")
                        Text("Próximas prioridades: ${review.priorities}")
                        if (review.answers.isNotBlank()) Text(review.answers, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showAddRecord && selectedType != null) {
        RecordDialog(selectedType!!, onDismiss = { showAddRecord = false }, onSave = { title, details, status, dueDate ->
            val recordType = selectedType!!
            if (recordType == "reminder") {
                val epochDay = dueDate.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it, moreDateFormat).toEpochDay() }.getOrNull() }
                val pending = PendingReminder(recordType, title, details, status, dueDate, epochDay)
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    pendingReminder = pending
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onSaveRecord(recordType, title, details, status, dueDate)
                    epochDay?.let { LocalReminderNotifications.schedule(context, title, it) }
                    showAddRecord = false
                }
            } else {
                onSaveRecord(recordType, title, details, status, dueDate)
                showAddRecord = false
            }
        })
    }
}

private data class PendingReminder(val type: String, val title: String, val details: String, val status: String, val dueDate: String, val epochDay: Long?)

@Composable
private fun RecordCard(record: JourneyRecordEntity, onDelete: () -> Unit, onChangeStatus: (() -> Unit)? = null) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Status: ${record.status}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            record.dueDateEpochDay?.let { Text("Prazo: ${LocalDate.ofEpochDay(it).format(moreDateFormat)}", style = MaterialTheme.typography.bodySmall) }
            if (record.details.isNotBlank()) Text(record.details, style = MaterialTheme.typography.bodyMedium)
            onChangeStatus?.let { change ->
                TextButton(onClick = change) { Text(if (record.recordType == "document") "Alternar checklist" else "Mover para próxima coluna") }
            }
            TextButton(onClick = onDelete) { Text("Excluir") }
        }
    }
}

@Composable
private fun RecordDialog(type: String, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    val fields = recordFields(type)
    var values by remember(type) { mutableStateOf(fields.associate { it.first to "" }) }
    var dueDate by remember { mutableStateOf("") }
    var status by remember(type) { mutableStateOf(when (type) { "project" -> "Backlog"; "document" -> "Pendente"; "application" -> "Preparando"; else -> "Planejado" }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo registro") },
        text = {
            Column(Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(journeyRecordTypes.firstOrNull { it.second == type }?.first.orEmpty(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(title, { title = it }, label = { Text("Nome ou título *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                fields.forEach { (key, label) ->
                    OutlinedTextField(values[key].orEmpty(), { value -> values = values + (key to value) }, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(status, { status = it }, label = { Text("Status") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Prazo (DD/MM/AAAA, opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title, fields.mapNotNull { (key, label) -> values[key].orEmpty().trim().takeIf(String::isNotBlank)?.let { "$label: $it" } }.joinToString("\n"), status, dueDate) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

private fun recordFields(type: String): List<Pair<String, String>> = when (type) {
    "experience" -> listOf("employer" to "Empresa", "role" to "Cargo e responsabilidades", "country" to "País", "contract" to "Tipo de contrato", "period" to "Período", "achievements" to "Resultados alcançados")
    "dev_experience" -> listOf("employer" to "Empresa ou cliente", "role" to "Cargo em desenvolvimento", "stack" to "Tecnologias e stack", "work" to "Responsabilidades técnicas", "impact" to "Resultados e impacto", "url" to "Link do trabalho (opcional)")
    "project" -> listOf("problem" to "Problema resolvido", "stack" to "Tecnologias", "role" to "Sua contribuição", "repo" to "Repositório ou demo", "result" to "Resultado")
    "education" -> listOf("institution" to "Instituição", "course" to "Curso", "level" to "Nível", "period" to "Período", "educationStatus" to "Situação")
    "language" -> listOf("current" to "Nível atual", "target" to "Nível desejado", "exam" to "Exame ou certificado", "weekly" to "Horas de estudo por semana")
    "study_plan" -> listOf("subject" to "Matéria ou habilidade", "goal" to "Objetivo mensurável", "weekly" to "Horas por semana", "method" to "Método e recursos", "review" to "Próxima revisão")
    "certification" -> listOf("issuer" to "Emissor", "credential" to "Credencial", "period" to "Data ou validade", "url" to "Link de validação")
    "university" -> listOf("country" to "País", "program" to "Curso ou programa", "language" to "Idioma de ensino", "tuition" to "Mensalidade/anuidade informada", "credits" to "Regras de aproveitamento de créditos", "source" to "Link oficial da universidade")
    "application" -> listOf("institution" to "Universidade", "program" to "Programa", "requirements" to "Requisitos principais", "fee" to "Taxa de candidatura", "source" to "Página oficial de candidatura")
    "document" -> listOf("issuer" to "Órgão emissor", "country" to "País", "validity" to "Validade", "translation" to "Tradução/apostila necessária?", "required" to "Obrigatório para qual etapa?", "source" to "Fonte oficial")
    "destination" -> listOf("country" to "País/cidade", "visa" to "Visto ou rota legal", "work" to "Permissão de trabalho", "cost" to "Custo mensal estimado", "source" to "Fontes oficiais consultadas")
    "reminder" -> listOf("purpose" to "O que precisa lembrar?", "notes" to "Detalhes")
    else -> listOf("details" to "Detalhes, links, resultados e fontes")
}

private fun nextProjectStatus(current: String): String = when (current) {
    "Backlog", "Planejado" -> "Em andamento"
    "Em andamento" -> "Revisão"
    "Revisão" -> "Concluído"
    else -> "Backlog"
}

@Composable
private fun QuarterlyReviewForm(onSave: (String, String, String) -> Unit) {
    val prompts = listOf(
        "Empregabilidade internacional", "Evolução da reserva financeira", "Evolução do inglês",
        "Evolução do italiano", "Projetos relevantes", "Prazos cumpridos", "Viabilidade do plano",
        "Maior risco", "O que parar de fazer",
    )
    var answers by remember { mutableStateOf(List(prompts.size) { "" }) }
    var priorities by remember { mutableStateOf("") }
    var risk by remember { mutableStateOf("") }
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Revisão do trimestre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            prompts.forEachIndexed { index, prompt ->
                OutlinedTextField(answers[index], { value -> answers = answers.toMutableList().also { it[index] = value } }, label = { Text(prompt) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
            OutlinedTextField(risk, { risk = it }, label = { Text("Maior risco atual") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(priorities, { priorities = it }, label = { Text("Três prioridades do próximo trimestre *") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(onClick = { onSave(prompts.zip(answers).filter { it.second.isNotBlank() }.joinToString("\n\n") { "${it.first}: ${it.second}" }, priorities, risk) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar revisão") }
        }
    }
}
