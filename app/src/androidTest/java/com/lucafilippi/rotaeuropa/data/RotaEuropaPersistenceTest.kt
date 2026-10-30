package com.lucafilippi.rotaeuropa.data

import androidx.room.Room
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
