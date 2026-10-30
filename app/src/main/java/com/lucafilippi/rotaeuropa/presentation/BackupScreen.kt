package com.lucafilippi.rotaeuropa.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun BackupScreen(viewModel: RotaEuropaViewModel, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var output by remember { mutableStateOf<String?>(null) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }

    val jsonExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val content = output
        if (uri != null && content != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(content) }
                ?: error("Não foi possível abrir o arquivo escolhido.")
        }.onSuccess { status = "Backup JSON salvo." }.onFailure { status = "Falha ao salvar o arquivo." }
        output = null
    }
    val csvExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val content = output
        if (uri != null && content != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(content) }
                ?: error("Não foi possível abrir o arquivo escolhido.")
        }.onSuccess { status = "CSV salvo." }.onFailure { status = "Falha ao salvar o arquivo." }
        output = null
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Não foi possível ler o arquivo.")
        }.onSuccess { content ->
            if (content.length > 20_000_000) status = "O arquivo excede o limite de 20 MB."
            else pendingImport = content
        }.onFailure { status = "Não foi possível ler esse arquivo." }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("BACKUP E EXPORTAÇÃO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Proteja seus dados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("O backup é salvo onde você escolher, como no armazenamento do celular ou em um local externo. O aplicativo não envia dados para a nuvem.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Backup completo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Inclui tarefas, fases, reserva, movimentações, orçamento, cotação, registros e revisões.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { viewModel.createJsonBackup { content -> output = content; jsonExporter.launch("ROTA-EUROPA-backup.json") } }, modifier = Modifier.fillMaxWidth()) { Text("Exportar backup JSON") }
                OutlinedButton(onClick = { importer.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Importar backup JSON") }
            }
        }
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Exportação para planilha", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Exporta tarefas, movimentações e custos em CSV, compatível com planilhas.", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { output = viewModel.createCsvExport(); csvExporter.launch("ROTA-EUROPA-dados.csv") }, modifier = Modifier.fillMaxWidth()) { Text("Exportar CSV") }
            }
        }
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary)
        OutlinedButton(onClick = onNavigateBack) { Text("Voltar") }
    }

    pendingImport?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Substituir dados atuais?") },
            text = { Text("A importação validará o arquivo e, se estiver íntegro, substituirá os dados locais por este backup. Salve um backup atual antes de continuar. Nenhum dado será substituído sem esta confirmação.") },
            confirmButton = { TextButton(onClick = { viewModel.importJsonBackup(backup); pendingImport = null; status = "Importação solicitada. Confira a mensagem de resultado." }) { Text("Substituir e importar") } },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancelar") } },
        )
    }
}
