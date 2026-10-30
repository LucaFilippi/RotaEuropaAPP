package com.lucafilippi.rotaeuropa.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "migration_scenarios")
data class MigrationScenarioEntity(
    @PrimaryKey val id: Long = DEFAULT_SCENARIO_ID,
    val name: String,
    val destination: String,
    val targetDateEpochDay: Long? = null,
    val active: Boolean = true,
)

@Entity(
    tableName = "plan_phases",
    foreignKeys = [ForeignKey(
        entity = MigrationScenarioEntity::class,
        parentColumns = ["id"], childColumns = ["scenarioId"], onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("scenarioId")],
)
data class PlanPhaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long = DEFAULT_SCENARIO_ID,
    val name: String,
    val order: Int,
)

@Entity(
    tableName = "plan_tasks",
    foreignKeys = [
        ForeignKey(
            entity = MigrationScenarioEntity::class,
            parentColumns = ["id"], childColumns = ["scenarioId"], onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlanPhaseEntity::class,
            parentColumns = ["id"], childColumns = ["phaseId"], onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("scenarioId"), Index("phaseId"), Index("dueDateEpochDay")],
)
data class PlanTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long = DEFAULT_SCENARIO_ID,
    val phaseId: Long,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String,
    val createdAtMillis: Long,
    val dueDateEpochDay: Long?,
    val completedAtMillis: Long?,
    val importance: Int,
    val required: Boolean,
)

@Entity(
    tableName = "financial_reserves",
    foreignKeys = [ForeignKey(
        entity = MigrationScenarioEntity::class,
        parentColumns = ["id"], childColumns = ["scenarioId"], onDelete = ForeignKey.CASCADE,
    )],
)
data class FinancialReserveEntity(
    @PrimaryKey val scenarioId: Long = DEFAULT_SCENARIO_ID,
    val currency: String = "EUR",
    val balanceMinor: Long = 0,
    val goalMinor: Long = 0,
    val monthlyContributionMinor: Long = 0,
)

@Entity(
    tableName = "financial_transactions",
    indices = [Index("scenarioId"), Index("occurredAtEpochDay")],
    foreignKeys = [ForeignKey(entity = MigrationScenarioEntity::class, parentColumns = ["id"], childColumns = ["scenarioId"], onDelete = ForeignKey.CASCADE)],
)
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long = DEFAULT_SCENARIO_ID,
    val amountMinor: Long,
    val currency: String,
    val kind: String,
    val occurredAtEpochDay: Long,
    val note: String,
)

@Entity(
    tableName = "budget_items",
    indices = [Index("scenarioId"), Index("category")],
    foreignKeys = [ForeignKey(entity = MigrationScenarioEntity::class, parentColumns = ["id"], childColumns = ["scenarioId"], onDelete = ForeignKey.CASCADE)],
)
data class BudgetItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long = DEFAULT_SCENARIO_ID,
    val name: String,
    val category: String,
    val amountMinor: Long,
    val currency: String,
    val recurring: Boolean,
    val note: String,
)

@Entity(
    tableName = "manual_exchange_rates",
    indices = [Index("scenarioId"), Index("quotedAtEpochDay")],
    foreignKeys = [ForeignKey(entity = MigrationScenarioEntity::class, parentColumns = ["id"], childColumns = ["scenarioId"], onDelete = ForeignKey.CASCADE)],
)
data class ManualExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long = DEFAULT_SCENARIO_ID,
    val brlPerEurMinor: Long,
    val quotedAtEpochDay: Long,
    val source: String,
    val note: String,
)

@Entity(
    tableName = "journey_records",
    indices = [Index("recordType"), Index("updatedAtMillis")],
)
data class JourneyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordType: String,
    val title: String,
    val details: String,
    val status: String,
    val dueDateEpochDay: Long? = null,
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "quarterly_reviews")
data class QuarterlyReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reviewedAtEpochDay: Long,
    val answers: String,
    val priorities: String,
    val risk: String,
)

const val DEFAULT_SCENARIO_ID = 1L
