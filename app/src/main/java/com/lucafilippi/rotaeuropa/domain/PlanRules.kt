package com.lucafilippi.rotaeuropa.domain

import java.math.BigDecimal
import java.math.RoundingMode

enum class TaskStatus(val label: String) {
    NOT_STARTED("Não iniciada"), IN_PROGRESS("Em andamento"), BLOCKED("Bloqueada"),
    COMPLETED("Concluída"), CANCELLED("Cancelada")
}

enum class TaskPriority(val label: String, val order: Int) {
    LOW("Baixa", 0), NORMAL("Normal", 1), HIGH("Alta", 2), CRITICAL("Crítica", 3)
}

data class ProgressSummary(
    val percent: Int,
    val completed: Int,
    val overdue: Int,
    val pendingRequired: Int,
    val total: Int,
)

fun calculateProgress(
    tasks: List<ProgressTask>,
    todayEpochDay: Long,
): ProgressSummary {
    val active = tasks.filter { it.status != TaskStatus.CANCELLED }
    val totalWeight = active.sumOf { it.importance.coerceIn(1, 5) }
    val completeWeight = active.filter { it.status == TaskStatus.COMPLETED }
        .sumOf { it.importance.coerceIn(1, 5) }
    val percent = if (totalWeight == 0) 0 else (completeWeight * 100 / totalWeight)
    return ProgressSummary(
        percent = percent,
        completed = active.count { it.status == TaskStatus.COMPLETED },
        overdue = active.count {
            it.status != TaskStatus.COMPLETED && it.dueDateEpochDay != null && it.dueDateEpochDay < todayEpochDay
        },
        pendingRequired = active.count { it.required && it.status != TaskStatus.COMPLETED },
        total = active.size,
    )
}

data class ProgressTask(
    val status: TaskStatus,
    val importance: Int,
    val dueDateEpochDay: Long? = null,
    val required: Boolean = false,
)

data class ReserveProjection(
    val remainingMinor: Long,
    val percent: Int,
    val monthsToGoal: Int?,
)

fun calculateReserveProjection(
    balanceMinor: Long,
    goalMinor: Long,
    monthlyContributionMinor: Long,
): ReserveProjection {
    val safeBalance = balanceMinor.coerceAtLeast(0)
    val safeGoal = goalMinor.coerceAtLeast(0)
    val remaining = (safeGoal - safeBalance).coerceAtLeast(0)
    val percent = when {
        safeGoal == 0L -> 0
        safeBalance >= safeGoal -> 100
        else -> BigDecimal.valueOf(safeBalance).multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(safeGoal), 0, RoundingMode.DOWN).toInt().coerceIn(0, 100)
    }
    val months = when {
        remaining == 0L -> 0
        monthlyContributionMinor <= 0L -> null
        else -> BigDecimal.valueOf(remaining)
            .divide(BigDecimal.valueOf(monthlyContributionMinor), 0, RoundingMode.CEILING)
            .let { it.min(BigDecimal.valueOf(Int.MAX_VALUE.toLong())).toInt() }
    }
    return ReserveProjection(remaining, percent, months)
}

fun parseMoneyToMinorUnits(input: String): Long? {
    val raw = input.trim().replace(" ", "")
    val normalized = when {
        raw.isBlank() -> return 0L
        raw.contains(',') && raw.contains('.') -> {
            if (!raw.matches(Regex("[0-9]{1,3}(\\.[0-9]{3})+,[0-9]{1,2}"))) return null
            raw.replace(".", "").replace(',', '.')
        }
        raw.count { it == ',' } == 1 && !raw.contains('.') -> raw.replace(',', '.')
        raw.count { it == '.' } <= 1 && !raw.contains(',') -> raw
        else -> return null
    }
    return runCatching {
        BigDecimal(normalized).setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2).longValueExact().takeIf { it >= 0 }
    }.getOrNull()
}

data class PlanTaskItem(
    val id: Long,
    val phaseId: Long,
    val title: String,
    val description: String,
    val category: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val createdAtMillis: Long,
    val dueDateEpochDay: Long?,
    val completedAtMillis: Long?,
    val importance: Int,
    val required: Boolean,
)

data class PlanPhaseItem(val id: Long, val name: String, val order: Int)

data class ReserveItem(
    val balanceMinor: Long,
    val goalMinor: Long,
    val monthlyContributionMinor: Long,
    val currency: String,
)

data class ScenarioItem(
    val name: String,
    val destination: String,
    val targetDateEpochDay: Long?,
    val active: Boolean = true,
    val id: Long = 1L,
)

data class ReadinessSummary(val score: Int, val blockers: List<String>)

fun calculateReadiness(
    destinationConfigured: Boolean,
    targetDateConfigured: Boolean,
    reserveGoalConfigured: Boolean,
    reserveGoalReached: Boolean,
    requiredTasksComplete: Boolean,
    documentsConfigured: Boolean,
    requiredDocumentsComplete: Boolean,
): ReadinessSummary {
    val checks = listOf(
        destinationConfigured to "Defina o destino principal.",
        targetDateConfigured to "Defina uma data-alvo.",
        (reserveGoalConfigured && reserveGoalReached) to "A reserva ainda não alcançou a meta.",
        requiredTasksComplete to "Conclua as tarefas obrigatórias em aberto.",
        (documentsConfigured && requiredDocumentsComplete) to "Revise o checklist de documentos obrigatórios.",
    )
    return ReadinessSummary(checks.count { it.first } * 20, checks.filterNot { it.first }.map { it.second })
}
