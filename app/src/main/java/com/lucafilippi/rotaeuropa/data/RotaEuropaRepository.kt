package com.lucafilippi.rotaeuropa.data

import androidx.room.withTransaction
import com.lucafilippi.rotaeuropa.data.local.DEFAULT_SCENARIO_ID
import com.lucafilippi.rotaeuropa.data.local.FinancialReserveEntity
import com.lucafilippi.rotaeuropa.data.local.FinancialTransactionEntity
import com.lucafilippi.rotaeuropa.data.local.BudgetItemEntity
import com.lucafilippi.rotaeuropa.data.local.ManualExchangeRateEntity
import com.lucafilippi.rotaeuropa.data.local.JourneyRecordEntity
import com.lucafilippi.rotaeuropa.data.local.QuarterlyReviewEntity
import com.lucafilippi.rotaeuropa.data.local.MigrationScenarioEntity
import com.lucafilippi.rotaeuropa.data.local.PlanPhaseEntity
import com.lucafilippi.rotaeuropa.data.local.PlanTaskEntity
import com.lucafilippi.rotaeuropa.data.local.RotaEuropaDatabase
import com.lucafilippi.rotaeuropa.domain.PlanPhaseItem
import com.lucafilippi.rotaeuropa.domain.PlanTaskItem
import com.lucafilippi.rotaeuropa.domain.ReserveItem
import com.lucafilippi.rotaeuropa.domain.FinancialTransactionItem
import com.lucafilippi.rotaeuropa.domain.TransactionKind
import com.lucafilippi.rotaeuropa.domain.BudgetItem
import com.lucafilippi.rotaeuropa.domain.ExchangeRateItem
import com.lucafilippi.rotaeuropa.domain.ScenarioItem
import com.lucafilippi.rotaeuropa.domain.TaskPriority
import com.lucafilippi.rotaeuropa.domain.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class ExtendedLocalData(
    val transactions: List<FinancialTransactionItem>,
    val budget: List<BudgetItem>,
    val exchangeRates: List<ExchangeRateItem>,
    val journeyRecords: List<JourneyRecordEntity>,
    val quarterlyReviews: List<QuarterlyReviewEntity>,
)

class RotaEuropaRepository(private val database: RotaEuropaDatabase) {
    private val scenarioDao = database.scenarioDao()
    private val phaseDao = database.phaseDao()
    private val taskDao = database.taskDao()
    private val reserveDao = database.reserveDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val exchangeRateDao = database.exchangeRateDao()
    private val journeyRecordDao = database.journeyRecordDao()
    private val quarterlyReviewDao = database.quarterlyReviewDao()

    val scenarios = scenarioDao.observeAll().map { rows -> rows.map { ScenarioItem(it.name, it.destination, it.targetDateEpochDay, it.active, it.id) } }
    private val activeScenario = scenarioDao.observeActive().map { it?.id ?: DEFAULT_SCENARIO_ID }
    val tasks: Flow<List<PlanTaskItem>> = activeScenario.flatMapLatest { id -> taskDao.observeForScenario(id) }.map { rows ->
        rows.map { it.toItem() }
    }
    val phases: Flow<List<PlanPhaseItem>> = activeScenario.flatMapLatest { id -> phaseDao.observeForScenario(id) }.map { rows ->
        rows.map { PlanPhaseItem(it.id, it.name, it.order) }
    }
    val reserve: Flow<ReserveItem> = activeScenario.flatMapLatest { id -> reserveDao.observe(id) }.map { row ->
        row?.let { ReserveItem(it.balanceMinor, it.goalMinor, it.monthlyContributionMinor, it.currency) }
            ?: ReserveItem(0, 0, 0, "EUR")
    }
    val scenario: Flow<ScenarioItem> = scenarioDao.observeActive().map { row ->
        row?.let { ScenarioItem(it.name, it.destination, it.targetDateEpochDay, it.active, it.id) }
            ?: ScenarioItem("Plano principal", "", null, id = DEFAULT_SCENARIO_ID)
    }
    val transactions = activeScenario.flatMapLatest { id -> transactionDao.observeForScenario(id) }.map { rows -> rows.map { it.toItem() } }
    val budget = activeScenario.flatMapLatest { id -> budgetDao.observeForScenario(id) }.map { rows -> rows.map { BudgetItem(it.id, it.name, it.category, it.amountMinor, it.currency, it.recurring, it.note) } }
    val exchangeRates = activeScenario.flatMapLatest { id -> exchangeRateDao.observeForScenario(id) }.map { rows -> rows.map { ExchangeRateItem(it.id, it.brlPerEurMinor, it.quotedAtEpochDay, it.source, it.note) } }
    fun journeyRecords(type: String) = journeyRecordDao.observe(type)
    val quarterlyReviews = quarterlyReviewDao.observeAll()
    val extendedData = combine(transactions, budget, exchangeRates, journeyRecordDao.observeAll(), quarterlyReviews) { tx, items, rates, records, reviews ->
        ExtendedLocalData(tx, items, rates, records, reviews)
    }

    suspend fun selectScenario(id: Long) = database.withTransaction {
        require(scenarioDao.get(id) != null) { "O cenário selecionado não existe." }
        scenarioDao.clearActive()
        scenarioDao.save(scenarioDao.get(id)!!.copy(active = true))
    }

    suspend fun createScenario(name: String, destination: String, targetDateEpochDay: Long?) = database.withTransaction {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "Informe um nome para o cenário." }
        val id = (scenarioDao.maxId() ?: 0L) + 1L
        scenarioDao.clearActive()
        scenarioDao.save(MigrationScenarioEntity(id = id, name = cleanName, destination = destination.trim(), targetDateEpochDay = targetDateEpochDay, active = true))
        phaseDao.insertAll(listOf("Fundação", "Consolidação", "Preparação da mudança", "Execução").mapIndexed { index, phase -> PlanPhaseEntity(scenarioId = id, name = phase, order = index) })
        reserveDao.insertIfMissing(FinancialReserveEntity(scenarioId = id))
    }

    private suspend fun activeId() = scenarioDao.getActive()?.id ?: DEFAULT_SCENARIO_ID

    suspend fun saveTransaction(item: FinancialTransactionItem) {
        val scenarioId = activeId()
        val reserve = reserveDao.get(scenarioId) ?: FinancialReserveEntity(scenarioId = scenarioId)
        require(item.amountMinor > 0) { "Informe um valor maior que zero." }
        require(item.currency == reserve.currency) { "Para atualizar o saldo, use a mesma moeda configurada para a reserva (${reserve.currency})." }
        if (item.kind == TransactionKind.WITHDRAWAL) require(item.amountMinor <= reserve.balanceMinor) { "A retirada não pode superar a reserva atual." }
        database.withTransaction {
            transactionDao.save(FinancialTransactionEntity(item.id, scenarioId, item.amountMinor, item.currency, item.kind.name, item.occurredAtEpochDay, item.note))
            if (item.id == 0L) {
                val change = item.amountMinor * item.kind.sign
                reserveDao.save(reserve.copy(balanceMinor = (reserve.balanceMinor + change).coerceAtLeast(0)))
            }
        }
    }

    suspend fun deleteTransaction(id: Long) {
        val scenarioId = activeId()
        database.withTransaction {
            val item = transactionDao.get(id, scenarioId) ?: return@withTransaction
            val reserve = reserveDao.get(scenarioId)
            if (reserve != null && item.currency == reserve.currency) {
                val sign = if (item.kind == TransactionKind.CONTRIBUTION.name) -1 else 1
                reserveDao.save(reserve.copy(balanceMinor = (reserve.balanceMinor + item.amountMinor * sign).coerceAtLeast(0)))
            }
            transactionDao.delete(id, scenarioId)
        }
    }

    suspend fun saveBudget(item: BudgetItem) = budgetDao.save(BudgetItemEntity(item.id, activeId(), item.name, item.category, item.amountMinor, item.currency, item.recurring, item.note))
    suspend fun deleteBudget(id: Long) = budgetDao.delete(id, activeId())
    suspend fun saveExchangeRate(item: ExchangeRateItem) = exchangeRateDao.save(ManualExchangeRateEntity(item.id, activeId(), item.brlPerEurMinor, item.quotedAtEpochDay, item.source, item.note))
    suspend fun saveJourneyRecord(item: JourneyRecordEntity) = journeyRecordDao.save(item)
    suspend fun deleteJourneyRecord(id: Long) = journeyRecordDao.delete(id)
    suspend fun saveQuarterlyReview(item: QuarterlyReviewEntity) = quarterlyReviewDao.save(item)

    suspend fun createJsonBackup(): String {
        val root = JSONObject().put("format", "rota-europa-backup").put("schemaVersion", 1)
            .put("createdAt", Instant.now().toString())
        root.put("scenarios", JSONArray(scenarioDao.all().map { JSONObject().put("id", it.id).put("name", it.name).put("destination", it.destination).put("targetDateEpochDay", it.targetDateEpochDay ?: JSONObject.NULL).put("active", it.active) }))
        root.put("phases", JSONArray(phaseDao.all().map { JSONObject().put("id", it.id).put("scenarioId", it.scenarioId).put("name", it.name).put("order", it.order) }))
        root.put("tasks", JSONArray(taskDao.all().map { JSONObject().put("id", it.id).put("scenarioId", it.scenarioId).put("phaseId", it.phaseId).put("title", it.title).put("description", it.description).put("category", it.category).put("priority", it.priority).put("status", it.status).put("createdAtMillis", it.createdAtMillis).put("dueDateEpochDay", it.dueDateEpochDay ?: JSONObject.NULL).put("completedAtMillis", it.completedAtMillis ?: JSONObject.NULL).put("importance", it.importance).put("required", it.required) }))
        root.put("reserves", JSONArray(reserveDao.all().map { JSONObject().put("scenarioId", it.scenarioId).put("currency", it.currency).put("balanceMinor", it.balanceMinor).put("goalMinor", it.goalMinor).put("monthlyContributionMinor", it.monthlyContributionMinor) }))
        root.put("transactions", JSONArray(transactionDao.all().map { JSONObject().put("id", it.id).put("scenarioId", it.scenarioId).put("amountMinor", it.amountMinor).put("currency", it.currency).put("kind", it.kind).put("occurredAtEpochDay", it.occurredAtEpochDay).put("note", it.note) }))
        root.put("budget", JSONArray(budgetDao.all().map { JSONObject().put("id", it.id).put("scenarioId", it.scenarioId).put("name", it.name).put("category", it.category).put("amountMinor", it.amountMinor).put("currency", it.currency).put("recurring", it.recurring).put("note", it.note) }))
        root.put("exchangeRates", JSONArray(exchangeRateDao.all().map { JSONObject().put("id", it.id).put("scenarioId", it.scenarioId).put("brlPerEurMinor", it.brlPerEurMinor).put("quotedAtEpochDay", it.quotedAtEpochDay).put("source", it.source).put("note", it.note) }))
        root.put("journeyRecords", JSONArray(journeyRecordDao.all().map { JSONObject().put("id", it.id).put("recordType", it.recordType).put("title", it.title).put("details", it.details).put("status", it.status).put("dueDateEpochDay", it.dueDateEpochDay ?: JSONObject.NULL).put("updatedAtMillis", it.updatedAtMillis) }))
        root.put("quarterlyReviews", JSONArray(quarterlyReviewDao.all().map { JSONObject().put("id", it.id).put("reviewedAtEpochDay", it.reviewedAtEpochDay).put("answers", it.answers).put("priorities", it.priorities).put("risk", it.risk) }))
        return root.toString(2)
    }

    suspend fun replaceFromJsonBackup(json: String) {
        val parsed = parseBackup(json)
        database.withTransaction {
            taskDao.clearAll()
            transactionDao.clearAll()
            budgetDao.clearAll()
            exchangeRateDao.clearAll()
            journeyRecordDao.clearAll()
            quarterlyReviewDao.clearAll()
            reserveDao.clearAll()
            phaseDao.clearAll()
            scenarioDao.clearAll()
            parsed.scenarios.forEach { scenarioDao.save(it) }
            phaseDao.insertAll(parsed.phases)
            parsed.tasks.forEach { taskDao.save(it) }
            parsed.reserves.forEach { reserveDao.save(it) }
            parsed.transactions.forEach { transactionDao.save(it) }
            parsed.budget.forEach { budgetDao.save(it) }
            parsed.exchangeRates.forEach { exchangeRateDao.save(it) }
            parsed.records.forEach { journeyRecordDao.save(it) }
            parsed.reviews.forEach { quarterlyReviewDao.save(it) }
        }
    }

    suspend fun mergeFromJsonBackup(json: String) {
        val parsed = parseBackup(json)
        database.withTransaction {
            val firstNewId = (scenarioDao.maxId() ?: 0L) + 1L
            val scenarioIdMap = parsed.scenarios.mapIndexed { index, scenario -> scenario.id to firstNewId + index }.toMap()
            parsed.scenarios.forEach { scenario ->
                scenarioDao.save(scenario.copy(id = scenarioIdMap.getValue(scenario.id), active = false))
            }
            phaseDao.insertAll(parsed.phases.map { phase -> phase.copy(id = 0L, scenarioId = scenarioIdMap.getValue(phase.scenarioId)) })
            val phaseIdMap = parsed.phases.associate { source ->
                val newScenarioId = scenarioIdMap.getValue(source.scenarioId)
                val created = phaseDao.getForScenario(newScenarioId).first { it.name == source.name && it.order == source.order }
                source.id to created.id
            }
            parsed.tasks.forEach { task -> taskDao.save(task.copy(id = 0L, scenarioId = scenarioIdMap.getValue(task.scenarioId), phaseId = phaseIdMap.getValue(task.phaseId))) }
            parsed.reserves.forEach { reserve -> reserveDao.save(reserve.copy(scenarioId = scenarioIdMap.getValue(reserve.scenarioId))) }
            parsed.transactions.forEach { item -> transactionDao.save(item.copy(id = 0L, scenarioId = scenarioIdMap.getValue(item.scenarioId))) }
            parsed.budget.forEach { item -> budgetDao.save(item.copy(id = 0L, scenarioId = scenarioIdMap.getValue(item.scenarioId))) }
            parsed.exchangeRates.forEach { item -> exchangeRateDao.save(item.copy(id = 0L, scenarioId = scenarioIdMap.getValue(item.scenarioId))) }
            parsed.records.forEach { journeyRecordDao.save(it.copy(id = 0L)) }
            parsed.reviews.forEach { quarterlyReviewDao.save(it.copy(id = 0L)) }
        }
    }

    private data class ParsedBackup(
        val scenarios: List<MigrationScenarioEntity>, val phases: List<PlanPhaseEntity>, val tasks: List<PlanTaskEntity>,
        val reserves: List<FinancialReserveEntity>, val transactions: List<FinancialTransactionEntity>, val budget: List<BudgetItemEntity>,
        val exchangeRates: List<ManualExchangeRateEntity>, val records: List<JourneyRecordEntity>, val reviews: List<QuarterlyReviewEntity>,
    )

    private fun parseBackup(json: String): ParsedBackup {
        require(json.length <= 20_000_000) { "O arquivo excede o limite de 20 MB." }
        val root = JSONObject(json)
        require(root.getString("format") == "rota-europa-backup") { "Este arquivo não é um backup do Rota Europa." }
        require(root.getInt("schemaVersion") == 1) { "Versão do backup não compatível." }
        fun rows(key: String) = root.getJSONArray(key)
        val scenarios = rows("scenarios").objects().map { MigrationScenarioEntity(it.getLong("id"), it.getString("name"), it.getString("destination"), it.nullableLong("targetDateEpochDay"), it.getBoolean("active")) }
        require(scenarios.isNotEmpty() && scenarios.all { it.id > 0 && it.name.isNotBlank() } && scenarios.map { it.id }.distinct().size == scenarios.size && scenarios.count { it.active } == 1) { "A lista de cenários do backup é inválida." }
        val scenarioIds = scenarios.map { it.id }.toSet()
        val phases = rows("phases").objects().map { PlanPhaseEntity(it.getLong("id"), it.getLong("scenarioId"), it.getString("name"), it.getInt("order")) }
        require(phases.isNotEmpty() && phases.all { it.scenarioId in scenarioIds && it.id > 0 && it.name.isNotBlank() } && phases.map { it.id }.distinct().size == phases.size && phases.map { Triple(it.scenarioId, it.name, it.order) }.distinct().size == phases.size) { "As fases do backup são inválidas." }
        val phaseIds = phases.map { it.id }.toSet()
        val tasks = rows("tasks").objects().map {
            val status = it.getString("status"); val priority = it.getString("priority")
            require(status in TaskStatus.entries.map { value -> value.name } && priority in TaskPriority.entries.map { value -> value.name }) { "O backup contém tarefa com status ou prioridade inválida." }
            PlanTaskEntity(it.getLong("id"), it.getLong("scenarioId"), it.getLong("phaseId"), it.getString("title"), it.getString("description"), it.getString("category"), priority, status, it.getLong("createdAtMillis"), it.nullableLong("dueDateEpochDay"), it.nullableLong("completedAtMillis"), it.getInt("importance"), it.getBoolean("required"))
        }
        val phaseScenarioById = phases.associate { it.id to it.scenarioId }
        require(tasks.all { it.scenarioId in scenarioIds && phaseScenarioById[it.phaseId] == it.scenarioId && it.title.isNotBlank() }) { "Há tarefas associadas a fases inexistentes ou sem título." }
        val reserves = rows("reserves").objects().map { FinancialReserveEntity(it.getLong("scenarioId"), it.getString("currency"), it.getLong("balanceMinor"), it.getLong("goalMinor"), it.getLong("monthlyContributionMinor")) }
        require(reserves.map { it.scenarioId }.toSet() == scenarioIds && reserves.size == scenarioIds.size && reserves.all { it.currency in listOf("BRL", "EUR") && it.balanceMinor >= 0 && it.goalMinor >= 0 && it.monthlyContributionMinor >= 0 }) { "Os dados das reservas são inválidos." }
        val transactions = rows("transactions").objects().map { FinancialTransactionEntity(it.getLong("id"), it.getLong("scenarioId"), it.getLong("amountMinor"), it.getString("currency"), it.getString("kind"), it.getLong("occurredAtEpochDay"), it.getString("note")) }
        require(transactions.all { it.scenarioId in scenarioIds && it.amountMinor > 0 && it.currency in listOf("BRL", "EUR") && it.kind in TransactionKind.entries.map { kind -> kind.name } }) { "Há movimentações financeiras inválidas." }
        val budget = rows("budget").objects().map { BudgetItemEntity(it.getLong("id"), it.getLong("scenarioId"), it.getString("name"), it.getString("category"), it.getLong("amountMinor"), it.getString("currency"), it.getBoolean("recurring"), it.getString("note")) }
        require(budget.all { it.scenarioId in scenarioIds && it.amountMinor > 0 && it.currency in listOf("BRL", "EUR") && it.name.isNotBlank() }) { "Há itens de orçamento inválidos." }
        val rates = rows("exchangeRates").objects().map { ManualExchangeRateEntity(it.getLong("id"), it.getLong("scenarioId"), it.getLong("brlPerEurMinor"), it.getLong("quotedAtEpochDay"), it.getString("source"), it.getString("note")) }
        require(rates.all { it.scenarioId in scenarioIds && it.brlPerEurMinor > 0 }) { "Há cotações inválidas." }
        val records = rows("journeyRecords").objects().map { JourneyRecordEntity(it.getLong("id"), it.getString("recordType"), it.getString("title"), it.getString("details"), it.getString("status"), it.nullableLong("dueDateEpochDay"), it.getLong("updatedAtMillis")) }
        require(records.all { it.recordType.isNotBlank() && it.title.isNotBlank() }) { "Há registros sem categoria ou título." }
        val reviews = rows("quarterlyReviews").objects().map { QuarterlyReviewEntity(it.getLong("id"), it.getLong("reviewedAtEpochDay"), it.getString("answers"), it.getString("priorities"), it.getString("risk")) }
        val allIds = listOf(phases.map { it.id }, tasks.map { it.id }, transactions.map { it.id }, budget.map { it.id }, rates.map { it.id }, records.map { it.id }, reviews.map { it.id })
        require(allIds.all { ids -> ids.size == ids.toSet().size && ids.all { it > 0 } }) { "O backup tem identificadores duplicados ou inválidos." }
        return ParsedBackup(scenarios, phases, tasks, reserves, transactions, budget, rates, records, reviews)
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun JSONObject.nullableLong(key: String): Long? = if (isNull(key)) null else getLong(key)

    suspend fun initialize() = database.withTransaction {
        scenarioDao.insertIfMissing(MigrationScenarioEntity(name = "Plano principal", destination = ""))
        if (scenarioDao.getActive() == null) scenarioDao.save((scenarioDao.get(DEFAULT_SCENARIO_ID) ?: MigrationScenarioEntity(name = "Plano principal", destination = "")).copy(active = true))
        if (phaseDao.countForScenario(DEFAULT_SCENARIO_ID) == 0) {
            phaseDao.insertAll(
                listOf(
                    PlanPhaseEntity(scenarioId = DEFAULT_SCENARIO_ID, name = "Fundação", order = 0),
                    PlanPhaseEntity(scenarioId = DEFAULT_SCENARIO_ID, name = "Consolidação", order = 1),
                    PlanPhaseEntity(scenarioId = DEFAULT_SCENARIO_ID, name = "Preparação da mudança", order = 2),
                    PlanPhaseEntity(scenarioId = DEFAULT_SCENARIO_ID, name = "Execução", order = 3),
                ),
            )
        }
        reserveDao.insertIfMissing(FinancialReserveEntity())
    }

    suspend fun saveTask(task: PlanTaskItem) {
        taskDao.save(
            PlanTaskEntity(
                id = task.id,
                scenarioId = activeId(),
                phaseId = task.phaseId,
                title = task.title,
                description = task.description,
                category = task.category,
                priority = task.priority.name,
                status = task.status.name,
                createdAtMillis = task.createdAtMillis,
                dueDateEpochDay = task.dueDateEpochDay,
                completedAtMillis = task.completedAtMillis,
                importance = task.importance,
                required = task.required,
            ),
        )
    }

    suspend fun deleteTask(id: Long) = taskDao.delete(id, activeId())

    suspend fun saveReserve(reserve: ReserveItem) {
        reserveDao.save(
            FinancialReserveEntity(
                scenarioId = activeId(),
                currency = reserve.currency,
                balanceMinor = reserve.balanceMinor,
                goalMinor = reserve.goalMinor,
                monthlyContributionMinor = reserve.monthlyContributionMinor,
            ),
        )
    }

    suspend fun saveTargetDate(epochDay: Long?) {
        val current = scenarioDao.getActive()
            ?: MigrationScenarioEntity(name = "Plano principal", destination = "")
        scenarioDao.save(current.copy(targetDateEpochDay = epochDay))
    }

    private fun PlanTaskEntity.toItem() = PlanTaskItem(
        id = id,
        phaseId = phaseId,
        title = title,
        description = description,
        category = category,
        priority = runCatching { TaskPriority.valueOf(priority) }.getOrDefault(TaskPriority.NORMAL),
        status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.NOT_STARTED),
        createdAtMillis = createdAtMillis,
        dueDateEpochDay = dueDateEpochDay,
        completedAtMillis = completedAtMillis,
        importance = importance,
        required = required,
    )

    private fun FinancialTransactionEntity.toItem() = FinancialTransactionItem(
        id, amountMinor, currency,
        runCatching { TransactionKind.valueOf(kind) }.getOrDefault(TransactionKind.CONTRIBUTION),
        occurredAtEpochDay, note,
    )
}
