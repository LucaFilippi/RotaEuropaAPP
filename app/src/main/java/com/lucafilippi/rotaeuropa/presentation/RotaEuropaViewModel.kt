package com.lucafilippi.rotaeuropa.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lucafilippi.rotaeuropa.data.RotaEuropaRepository
import com.lucafilippi.rotaeuropa.data.UserPreferencesRepository
import com.lucafilippi.rotaeuropa.data.ExtendedLocalData
import com.lucafilippi.rotaeuropa.data.local.JourneyRecordEntity
import com.lucafilippi.rotaeuropa.data.local.QuarterlyReviewEntity
import com.lucafilippi.rotaeuropa.domain.FinancialTransactionItem
import com.lucafilippi.rotaeuropa.domain.TransactionKind
import com.lucafilippi.rotaeuropa.domain.BudgetItem
import com.lucafilippi.rotaeuropa.domain.ExchangeRateItem
import com.lucafilippi.rotaeuropa.domain.PlanPhaseItem
import com.lucafilippi.rotaeuropa.domain.PlanTaskItem
import com.lucafilippi.rotaeuropa.domain.ProgressSummary
import com.lucafilippi.rotaeuropa.domain.ProgressTask
import com.lucafilippi.rotaeuropa.domain.ReserveItem
import com.lucafilippi.rotaeuropa.domain.ScenarioItem
import com.lucafilippi.rotaeuropa.domain.TaskPriority
import com.lucafilippi.rotaeuropa.domain.TaskStatus
import com.lucafilippi.rotaeuropa.domain.calculateProgress
import com.lucafilippi.rotaeuropa.domain.parseMoneyToMinorUnits
import java.time.LocalDate
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class AppUiState(
    val tasks: List<PlanTaskItem> = emptyList(),
    val phases: List<PlanPhaseItem> = emptyList(),
    val reserve: ReserveItem = ReserveItem(0, 0, 0, "EUR"),
    val scenario: ScenarioItem = ScenarioItem("Plano principal", "", null),
    val darkTheme: Boolean = false,
    val initialized: Boolean = false,
    val errorMessage: String? = null,
    val extendedData: ExtendedLocalData = ExtendedLocalData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
) {
    val progress: ProgressSummary
        get() = calculateProgress(
            tasks.map { ProgressTask(it.status, it.importance, it.dueDateEpochDay, it.required) },
            LocalDate.now().toEpochDay(),
        )
}

class RotaEuropaViewModel(
    private val repository: RotaEuropaRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale("pt", "BR"))
        .withResolverStyle(ResolverStyle.STRICT)

    init {
        viewModelScope.launch {
            runCatching { repository.initialize() }
                .onFailure { setError("Não foi possível preparar os dados locais: ${it.localizedMessage.orEmpty()}") }
            combine(repository.tasks, repository.phases, repository.reserve, repository.scenario, preferences.darkTheme) {
                    tasks, phases, reserve, scenario, darkTheme ->
                AppUiState(tasks, phases, reserve, scenario, darkTheme, initialized = true, errorMessage = _uiState.value.errorMessage)
            }.flatMapLatest { base ->
                repository.extendedData.map { base.copy(extendedData = it) }
            }.collect { _uiState.value = it }
        }
    }

    fun saveTask(
        id: Long,
        phaseId: Long,
        title: String,
        description: String,
        category: String,
        priority: TaskPriority,
        status: TaskStatus,
        dueDateText: String,
        importance: Int,
        required: Boolean,
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) { setError("Informe um título para a tarefa."); return false }
        if (phaseId == 0L) { setError("Selecione uma fase."); return false }
        val dueDate = if (dueDateText.isBlank()) null else runCatching {
            LocalDate.parse(dueDateText.trim(), dateFormatter).toEpochDay()
        }.getOrNull()
        if (!dueDateText.isBlank() && dueDate == null) {
            setError("Use uma data válida no formato DD/MM/AAAA.")
            return false
        }

        viewModelScope.launch {
            val previous = _uiState.value.tasks.firstOrNull { it.id == id }
            val now = System.currentTimeMillis()
            runCatching {
                repository.saveTask(
                    PlanTaskItem(
                        id = id,
                        phaseId = phaseId,
                        title = cleanTitle,
                        description = description.trim(),
                        category = category.trim().ifBlank { "Geral" },
                        priority = priority,
                        status = status,
                        createdAtMillis = previous?.createdAtMillis ?: now,
                        dueDateEpochDay = dueDate,
                        completedAtMillis = if (status == TaskStatus.COMPLETED) previous?.completedAtMillis ?: now else null,
                        importance = importance.coerceIn(1, 5),
                        required = required,
                    ),
                )
                clearError()
            }.onFailure { setError("Não foi possível salvar a tarefa: ${it.localizedMessage.orEmpty()}") }
        }
        return true
    }

    fun toggleTask(task: PlanTaskItem) {
        saveTask(
            task.id, task.phaseId, task.title, task.description, task.category, task.priority,
            if (task.status == TaskStatus.COMPLETED) TaskStatus.NOT_STARTED else TaskStatus.COMPLETED,
            task.dueDateEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFormatter) }.orEmpty(),
            task.importance, task.required,
        )
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteTask(id) }
                .onFailure { setError("Não foi possível excluir a tarefa.") }
        }
    }

    fun saveReserve(balance: String, goal: String, monthlyContribution: String, currency: String) {
        val balanceMinor = parseMoneyToMinorUnits(balance) ?: return setError("Confira o valor da reserva.")
        val goalMinor = parseMoneyToMinorUnits(goal) ?: return setError("Confira o valor da meta.")
        val monthlyMinor = parseMoneyToMinorUnits(monthlyContribution)
            ?: return setError("Confira o valor do aporte mensal.")
        viewModelScope.launch {
            runCatching { repository.saveReserve(ReserveItem(balanceMinor, goalMinor, monthlyMinor, currency)) }
                .onSuccess { clearError() }
                .onFailure { setError("Não foi possível salvar os dados financeiros.") }
        }
    }

    fun saveTransaction(amount: String, kind: TransactionKind, note: String, dateText: String = LocalDate.now().format(dateFormatter)) {
        val parsed = parseMoneyToMinorUnits(amount) ?: return setError("Confira o valor da movimentação.")
        if (parsed <= 0) return setError("Informe um valor maior que zero.")
        val epochDay = runCatching { LocalDate.parse(dateText, dateFormatter).toEpochDay() }.getOrNull()
            ?: return setError("Use uma data válida no formato DD/MM/AAAA.")
        viewModelScope.launch {
            runCatching {
                repository.saveTransaction(FinancialTransactionItem(amountMinor = parsed, currency = _uiState.value.reserve.currency, kind = kind, occurredAtEpochDay = epochDay, note = note.trim()))
            }.onSuccess { clearError() }.onFailure { setError(it.message ?: "Não foi possível salvar a movimentação.") }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { runCatching { repository.deleteTransaction(id) }.onFailure { setError("Não foi possível remover a movimentação.") } }
    }

    fun saveBudget(name: String, category: String, amount: String, currency: String, recurring: Boolean, note: String) {
        val parsed = parseMoneyToMinorUnits(amount) ?: return setError("Confira o valor do custo.")
        if (name.isBlank() || parsed <= 0) return setError("Informe nome e valor maior que zero.")
        viewModelScope.launch {
            runCatching { repository.saveBudget(BudgetItem(name = name.trim(), category = category, amountMinor = parsed, currency = currency, recurring = recurring, note = note.trim())) }
                .onSuccess { clearError() }.onFailure { setError("Não foi possível salvar o custo.") }
        }
    }

    fun deleteBudget(id: Long) { viewModelScope.launch { runCatching { repository.deleteBudget(id) }.onFailure { setError("Não foi possível remover o custo.") } } }

    fun saveExchangeRate(rateText: String, dateText: String, source: String, note: String) {
        val rate = parseMoneyToMinorUnits(rateText) ?: return setError("Confira a cotação (reais por euro).")
        if (rate == 0L) return setError("A cotação deve ser maior que zero.")
        val day = runCatching { LocalDate.parse(dateText, dateFormatter).toEpochDay() }.getOrNull()
            ?: return setError("Use uma data válida no formato DD/MM/AAAA.")
        viewModelScope.launch {
            runCatching { repository.saveExchangeRate(ExchangeRateItem(brlPerEurMinor = rate, quotedAtEpochDay = day, source = source.trim(), note = note.trim())) }
                .onSuccess { clearError() }.onFailure { setError("Não foi possível salvar a cotação.") }
        }
    }

    fun saveJourneyRecord(type: String, title: String, details: String, status: String, dueDateText: String) {
        if (title.isBlank()) return setError("Informe um título.")
        val dueDate = if (dueDateText.isBlank()) null else runCatching { LocalDate.parse(dueDateText, dateFormatter).toEpochDay() }.getOrNull()
            ?: if (dueDateText.isBlank()) null else return setError("Use uma data válida no formato DD/MM/AAAA.")
        viewModelScope.launch {
            runCatching { repository.saveJourneyRecord(JourneyRecordEntity(recordType = type, title = title.trim(), details = details.trim(), status = status, dueDateEpochDay = dueDate)) }
                .onSuccess { clearError() }.onFailure { setError("Não foi possível salvar o registro.") }
        }
    }

    fun deleteJourneyRecord(id: Long) { viewModelScope.launch { runCatching { repository.deleteJourneyRecord(id) }.onFailure { setError("Não foi possível remover o registro.") } } }

    fun saveQuarterlyReview(answers: String, priorities: String, risk: String) {
        if (priorities.isBlank()) return setError("Registre pelo menos uma prioridade para o próximo trimestre.")
        viewModelScope.launch {
            runCatching { repository.saveQuarterlyReview(QuarterlyReviewEntity(reviewedAtEpochDay = LocalDate.now().toEpochDay(), answers = answers, priorities = priorities.trim(), risk = risk.trim())) }
                .onSuccess { clearError() }.onFailure { setError("Não foi possível salvar a revisão.") }
        }
    }

    fun createJsonBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.createJsonBackup() }
                .onSuccess(onReady)
                .onFailure { setError("Não foi possível preparar o backup JSON.") }
        }
    }

    fun importJsonBackup(json: String) {
        viewModelScope.launch {
            runCatching { repository.replaceFromJsonBackup(json) }
                .onSuccess { clearError() }
                .onFailure { setError("Backup não importado: ${it.message ?: "arquivo inválido"}") }
        }
    }

    fun createCsvExport(): String {
        fun csv(value: String) = "\"${value.replace("\"", "\"\"")}\""
        val lines = mutableListOf("Tipo,Nome,Status,Moeda,Valor,Data,Observação")
        _uiState.value.tasks.forEach { task -> lines += listOf("Tarefa", task.title, task.status.label, "", "", task.dueDateEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFormatter) }.orEmpty(), task.description).joinToString(",", transform = ::csv) }
        _uiState.value.extendedData.transactions.forEach { item -> lines += listOf("${item.kind.label}", item.note, "Registrada", item.currency, BigDecimal.valueOf(item.amountMinor, 2).toPlainString(), LocalDate.ofEpochDay(item.occurredAtEpochDay).format(dateFormatter), item.note).joinToString(",", transform = ::csv) }
        _uiState.value.extendedData.budget.forEach { item -> lines += listOf("Custo", item.name, if (item.recurring) "Recorrente" else "Único", item.currency, BigDecimal.valueOf(item.amountMinor, 2).toPlainString(), "", item.note).joinToString(",", transform = ::csv) }
        return lines.joinToString("\r\n")
    }

    fun saveTargetDate(text: String) {
        val epochDay = if (text.isBlank()) null else runCatching {
            LocalDate.parse(text.trim(), dateFormatter).toEpochDay()
        }.getOrNull() ?: if (text.isBlank()) null else return setError("Use uma data válida no formato DD/MM/AAAA.")
        viewModelScope.launch {
            runCatching { repository.saveTargetDate(epochDay) }
                .onSuccess { clearError() }
                .onFailure { setError("Não foi possível salvar a data-alvo.") }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { runCatching { preferences.setDarkTheme(enabled) } }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
    private fun setError(message: String) { _uiState.value = _uiState.value.copy(errorMessage = message) }
}

class RotaEuropaViewModelFactory(
    private val repository: RotaEuropaRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RotaEuropaViewModel::class.java)) {
            return RotaEuropaViewModel(repository, preferences) as T
        }
        error("ViewModel desconhecida: ${modelClass.name}")
    }
}
