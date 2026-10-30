package com.lucafilippi.rotaeuropa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MigrationScenarioEntity::class,
        PlanPhaseEntity::class,
        PlanTaskEntity::class,
        FinancialReserveEntity::class,
        FinancialTransactionEntity::class,
        BudgetItemEntity::class,
        ManualExchangeRateEntity::class,
        JourneyRecordEntity::class,
        QuarterlyReviewEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class RotaEuropaDatabase : RoomDatabase() {
    abstract fun scenarioDao(): ScenarioDao
    abstract fun phaseDao(): PhaseDao
    abstract fun taskDao(): TaskDao
    abstract fun reserveDao(): ReserveDao
    abstract fun transactionDao(): FinancialTransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun journeyRecordDao(): JourneyRecordDao
    abstract fun quarterlyReviewDao(): QuarterlyReviewDao

    companion object {
        @Volatile private var instance: RotaEuropaDatabase? = null

        fun get(context: Context): RotaEuropaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RotaEuropaDatabase::class.java,
                "rota_europa.db",
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS financial_transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, scenarioId INTEGER NOT NULL, amountMinor INTEGER NOT NULL, currency TEXT NOT NULL, kind TEXT NOT NULL, occurredAtEpochDay INTEGER NOT NULL, note TEXT NOT NULL, FOREIGN KEY(scenarioId) REFERENCES migration_scenarios(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_scenarioId ON financial_transactions(scenarioId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_occurredAtEpochDay ON financial_transactions(occurredAtEpochDay)")
                db.execSQL("CREATE TABLE IF NOT EXISTS budget_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, scenarioId INTEGER NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL, amountMinor INTEGER NOT NULL, currency TEXT NOT NULL, recurring INTEGER NOT NULL, note TEXT NOT NULL, FOREIGN KEY(scenarioId) REFERENCES migration_scenarios(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_items_scenarioId ON budget_items(scenarioId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_items_category ON budget_items(category)")
                db.execSQL("CREATE TABLE IF NOT EXISTS manual_exchange_rates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, scenarioId INTEGER NOT NULL, brlPerEurMinor INTEGER NOT NULL, quotedAtEpochDay INTEGER NOT NULL, source TEXT NOT NULL, note TEXT NOT NULL, FOREIGN KEY(scenarioId) REFERENCES migration_scenarios(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_manual_exchange_rates_scenarioId ON manual_exchange_rates(scenarioId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_manual_exchange_rates_quotedAtEpochDay ON manual_exchange_rates(quotedAtEpochDay)")
                db.execSQL("CREATE TABLE IF NOT EXISTS journey_records (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, recordType TEXT NOT NULL, title TEXT NOT NULL, details TEXT NOT NULL, status TEXT NOT NULL, dueDateEpochDay INTEGER, updatedAtMillis INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journey_records_recordType ON journey_records(recordType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journey_records_updatedAtMillis ON journey_records(updatedAtMillis)")
                db.execSQL("CREATE TABLE IF NOT EXISTS quarterly_reviews (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, reviewedAtEpochDay INTEGER NOT NULL, answers TEXT NOT NULL, priorities TEXT NOT NULL, risk TEXT NOT NULL)")
            }
        }
    }
}
