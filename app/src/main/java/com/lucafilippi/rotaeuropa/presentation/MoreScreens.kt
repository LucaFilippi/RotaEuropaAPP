package com.lucafilippi.rotaeuropa.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucafilippi.rotaeuropa.data.local.JourneyRecordEntity
import com.lucafilippi.rotaeuropa.domain.journeyRecordTypes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val moreDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun MoreScreen(
    state: AppUiState,
    onSaveRecord: (String, String, String, String, String) -> Unit,
    onDeleteRecord: (Long) -> Unit,
    onSaveReview: (String, String, String) -> Unit,
    onOpenBackup: () -> Unit,
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showReview by remember { mutableStateOf(false) }
    var showAddRecord by remember { mutableStateOf(false) }
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
            records.forEach { RecordCard(it, onDelete = { onDeleteRecord(it.id) }) }
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
            onSaveRecord(selectedType!!, title, details, status, dueDate)
            showAddRecord = false
        })
    }
}

@Composable
private fun RecordCard(record: JourneyRecordEntity, onDelete: () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Status: ${record.status}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            record.dueDateEpochDay?.let { Text("Prazo: ${LocalDate.ofEpochDay(it).format(moreDateFormat)}", style = MaterialTheme.typography.bodySmall) }
            if (record.details.isNotBlank()) Text(record.details, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDelete) { Text("Excluir") }
        }
    }
}

@Composable
private fun RecordDialog(type: String, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Planejado") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo registro") },
        text = {
            Column(Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(journeyRecordTypes.firstOrNull { it.second == type }?.first.orEmpty(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(title, { title = it }, label = { Text("Nome ou título *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(details, { details = it }, label = { Text("Detalhes, links, resultados e fontes") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(status, { status = it }, label = { Text("Status") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dueDate, { dueDate = it }, label = { Text("Prazo (DD/MM/AAAA, opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(title, details, status, dueDate) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
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
