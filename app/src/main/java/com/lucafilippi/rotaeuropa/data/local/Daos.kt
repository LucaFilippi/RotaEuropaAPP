package com.lucafilippi.rotaeuropa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM migration_scenarios WHERE id = :id LIMIT 1")
    fun observe(id: Long): Flow<MigrationScenarioEntity?>

    @Query("SELECT * FROM migration_scenarios WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): MigrationScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(scenario: MigrationScenarioEntity)

    @Upsert
    suspend fun save(scenario: MigrationScenarioEntity)
    @Query("SELECT * FROM migration_scenarios ORDER BY id") suspend fun all(): List<MigrationScenarioEntity>
}

@Dao
interface PhaseDao {
    @Query("SELECT * FROM plan_phases WHERE scenarioId = :scenarioId ORDER BY `order`, id")
    fun observeForScenario(scenarioId: Long): Flow<List<PlanPhaseEntity>>

    @Query("SELECT COUNT(*) FROM plan_phases WHERE scenarioId = :scenarioId")
    suspend fun countForScenario(scenarioId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(phases: List<PlanPhaseEntity>)
    @Query("SELECT * FROM plan_phases ORDER BY scenarioId, `order`, id") suspend fun all(): List<PlanPhaseEntity>
    @Query("DELETE FROM plan_phases WHERE scenarioId = :scenarioId") suspend fun clearForScenario(scenarioId: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM plan_tasks WHERE scenarioId = :scenarioId ORDER BY (dueDateEpochDay IS NULL), dueDateEpochDay, createdAtMillis DESC")
    fun observeForScenario(scenarioId: Long): Flow<List<PlanTaskEntity>>

    @Upsert
    suspend fun save(task: PlanTaskEntity)
    @Query("SELECT * FROM plan_tasks ORDER BY id") suspend fun all(): List<PlanTaskEntity>
    @Query("DELETE FROM plan_tasks") suspend fun clearAll()

    @Query("DELETE FROM plan_tasks WHERE id = :id AND scenarioId = :scenarioId")
    suspend fun delete(id: Long, scenarioId: Long)
}

@Dao
interface ReserveDao {
    @Query("SELECT * FROM financial_reserves WHERE scenarioId = :scenarioId LIMIT 1")
    fun observe(scenarioId: Long): Flow<FinancialReserveEntity?>

    @Query("SELECT * FROM financial_reserves WHERE scenarioId = :scenarioId LIMIT 1")
    suspend fun get(scenarioId: Long): FinancialReserveEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(reserve: FinancialReserveEntity)

    @Upsert
    suspend fun save(reserve: FinancialReserveEntity)
    @Query("SELECT * FROM financial_reserves ORDER BY scenarioId") suspend fun all(): List<FinancialReserveEntity>
    @Query("DELETE FROM financial_reserves") suspend fun clearAll()
}

@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions WHERE scenarioId = :scenarioId ORDER BY occurredAtEpochDay DESC, id DESC")
    fun observeForScenario(scenarioId: Long): Flow<List<FinancialTransactionEntity>>

    @Upsert suspend fun save(item: FinancialTransactionEntity)
    @Query("SELECT * FROM financial_transactions WHERE id = :id AND scenarioId = :scenarioId LIMIT 1") suspend fun get(id: Long, scenarioId: Long): FinancialTransactionEntity?
    @Query("DELETE FROM financial_transactions WHERE id = :id AND scenarioId = :scenarioId") suspend fun delete(id: Long, scenarioId: Long)
    @Query("DELETE FROM financial_transactions WHERE scenarioId = :scenarioId") suspend fun clearForScenario(scenarioId: Long)
    @Query("SELECT * FROM financial_transactions ORDER BY id") suspend fun all(): List<FinancialTransactionEntity>
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_items WHERE scenarioId = :scenarioId ORDER BY category, id")
    fun observeForScenario(scenarioId: Long): Flow<List<BudgetItemEntity>>

    @Upsert suspend fun save(item: BudgetItemEntity)
    @Query("DELETE FROM budget_items WHERE id = :id AND scenarioId = :scenarioId") suspend fun delete(id: Long, scenarioId: Long)
    @Query("DELETE FROM budget_items WHERE scenarioId = :scenarioId") suspend fun clearForScenario(scenarioId: Long)
    @Query("SELECT * FROM budget_items ORDER BY id") suspend fun all(): List<BudgetItemEntity>
}

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM manual_exchange_rates WHERE scenarioId = :scenarioId ORDER BY quotedAtEpochDay DESC, id DESC")
    fun observeForScenario(scenarioId: Long): Flow<List<ManualExchangeRateEntity>>

    @Upsert suspend fun save(item: ManualExchangeRateEntity)
    @Query("DELETE FROM manual_exchange_rates WHERE scenarioId = :scenarioId") suspend fun clearForScenario(scenarioId: Long)
    @Query("SELECT * FROM manual_exchange_rates ORDER BY id") suspend fun all(): List<ManualExchangeRateEntity>
}

@Dao
interface JourneyRecordDao {
    @Query("SELECT * FROM journey_records WHERE recordType = :recordType ORDER BY (dueDateEpochDay IS NULL), dueDateEpochDay, updatedAtMillis DESC")
    fun observe(recordType: String): Flow<List<JourneyRecordEntity>>

    @Query("SELECT * FROM journey_records ORDER BY id") suspend fun all(): List<JourneyRecordEntity>
    @Query("SELECT * FROM journey_records ORDER BY (dueDateEpochDay IS NULL), dueDateEpochDay, updatedAtMillis DESC") fun observeAll(): Flow<List<JourneyRecordEntity>>
    @Upsert suspend fun save(item: JourneyRecordEntity)
    @Query("DELETE FROM journey_records WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM journey_records") suspend fun clearAll()
}

@Dao
interface QuarterlyReviewDao {
    @Query("SELECT * FROM quarterly_reviews ORDER BY reviewedAtEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<QuarterlyReviewEntity>>

    @Upsert suspend fun save(item: QuarterlyReviewEntity)
    @Query("DELETE FROM quarterly_reviews") suspend fun clearAll()
    @Query("SELECT * FROM quarterly_reviews ORDER BY id") suspend fun all(): List<QuarterlyReviewEntity>
}
