package com.lucafilippi.rotaeuropa.data

import androidx.room.Room
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lucafilippi.rotaeuropa.data.local.MigrationScenarioEntity
import com.lucafilippi.rotaeuropa.data.local.PlanPhaseEntity
import com.lucafilippi.rotaeuropa.data.local.PlanTaskEntity
import com.lucafilippi.rotaeuropa.data.local.RotaEuropaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotaEuropaPersistenceTest {
    @Test
    fun versionOneDatabaseMigratesWithoutLosingScenarioAndPlan() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "rota-migration-test.db"
        context.deleteDatabase(databaseName)
        val legacy = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        legacy.execSQL("CREATE TABLE migration_scenarios (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, destination TEXT NOT NULL, targetDateEpochDay INTEGER, active INTEGER NOT NULL)")
        legacy.execSQL("CREATE TABLE plan_phases (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, scenarioId INTEGER NOT NULL, name TEXT NOT NULL, `order` INTEGER NOT NULL, FOREIGN KEY(scenarioId) REFERENCES migration_scenarios(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        legacy.execSQL("CREATE INDEX index_plan_phases_scenarioId ON plan_phases(scenarioId)")
        legacy.execSQL("CREATE TABLE plan_tasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, scenarioId INTEGER NOT NULL, phaseId INTEGER NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, category TEXT NOT NULL, priority TEXT NOT NULL, status TEXT NOT NULL, createdAtMillis INTEGER NOT NULL, dueDateEpochDay INTEGER, completedAtMillis INTEGER, importance INTEGER NOT NULL, required INTEGER NOT NULL, FOREIGN KEY(scenarioId) REFERENCES migration_scenarios(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(phaseId) REFERENCES plan_phases(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
        legacy.execSQL("CREATE INDEX index_plan_tasks_scenarioId ON plan_tasks(scenarioId)")
        legacy.execSQL("CREATE INDEX index_plan_tasks_phaseId ON plan_tasks(phaseId)")
        legacy.execSQL("CREATE INDEX index_plan_tasks_dueDateEpochDay ON plan_tasks(dueDateEpochDay)")
        legacy.execSQL("CREATE TABLE financial_reserves (scenarioId INTEGER NOT NULL PRIMARY KEY, currency TEXT NOT NULL, balanceMinor INTEGER NOT NULL, goalMinor INTEGER NOT NULL, monthlyContributionMinor INTEGER NOT NULL, FOREIGN KEY(scenarioId) REFERENCES migration_scenarios(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        legacy.execSQL("INSERT INTO migration_scenarios VALUES (1, 'Plano existente', 'Roma', 21000, 1)")
        legacy.execSQL("INSERT INTO plan_phases(scenarioId, name, `order`) VALUES (1, 'Fase preservada', 0)")
        legacy.execSQL("INSERT INTO financial_reserves VALUES (1, 'EUR', 120000, 500000, 25000)")
        legacy.version = 1
        legacy.close()

        val migrated = Room.databaseBuilder(context, RotaEuropaDatabase::class.java, databaseName)
            .addMigrations(RotaEuropaDatabase.MIGRATION_1_2)
            .build()
        assertEquals(2, migrated.openHelper.readableDatabase.version)
        assertEquals("Plano existente", runBlocking { migrated.scenarioDao().get(1)?.name })
        assertEquals(1, runBlocking { migrated.phaseDao().countForScenario(1) })
        assertEquals(120_000L, runBlocking { migrated.reserveDao().get(1)?.balanceMinor })
        migrated.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun taskSurvivesClosingAndReopeningTheDatabase() {
        runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "rota-persistence-test.db"
        context.deleteDatabase(databaseName)

        var database = Room.databaseBuilder(context, RotaEuropaDatabase::class.java, databaseName).build()
        database.scenarioDao().save(MigrationScenarioEntity(id = 42, name = "Teste", destination = ""))
        database.phaseDao().insertAll(listOf(PlanPhaseEntity(scenarioId = 42, name = "Fundação", order = 0)))
        val phase = database.phaseDao().observeForScenario(42).first().single()
        database.taskDao().save(
            PlanTaskEntity(
                scenarioId = 42,
                phaseId = phase.id,
                title = "Tarefa persistida",
                description = "",
                category = "Geral",
                priority = "HIGH",
                status = "NOT_STARTED",
                createdAtMillis = 1,
                dueDateEpochDay = null,
                completedAtMillis = null,
                importance = 4,
                required = true,
            ),
        )
        database.close()

        database = Room.databaseBuilder(context, RotaEuropaDatabase::class.java, databaseName).build()
        val restored = database.taskDao().observeForScenario(42).first().single()

        assertEquals("Tarefa persistida", restored.title)
        assertEquals(4, restored.importance)
        assertEquals(true, restored.required)

        database.close()
        context.deleteDatabase(databaseName)
        }
    }
}
