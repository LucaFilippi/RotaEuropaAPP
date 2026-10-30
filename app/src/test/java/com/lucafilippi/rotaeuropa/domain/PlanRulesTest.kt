package com.lucafilippi.rotaeuropa.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlanRulesTest {
    @Test
    fun progressIsWeightedAndCancelledTasksAreIgnored() {
        val result = calculateProgress(
            listOf(
                ProgressTask(TaskStatus.COMPLETED, importance = 5),
                ProgressTask(TaskStatus.NOT_STARTED, importance = 1, required = true),
                ProgressTask(TaskStatus.CANCELLED, importance = 5, required = true),
            ),
            todayEpochDay = 100,
        )

        assertEquals(83, result.percent)
        assertEquals(1, result.completed)
        assertEquals(1, result.pendingRequired)
        assertEquals(2, result.total)
    }

    @Test
    fun overdueTaskMustBeOpenAndHavePastDeadline() {
        val result = calculateProgress(
            listOf(
                ProgressTask(TaskStatus.NOT_STARTED, 3, dueDateEpochDay = 99),
                ProgressTask(TaskStatus.BLOCKED, 2, dueDateEpochDay = 100),
                ProgressTask(TaskStatus.COMPLETED, 3, dueDateEpochDay = 20),
                ProgressTask(TaskStatus.NOT_STARTED, 3, dueDateEpochDay = null),
            ),
            todayEpochDay = 100,
        )

        assertEquals(1, result.overdue)
    }

    @Test
    fun reserveProjectionHandlesEmptyReachedAndUnfundedGoals() {
        assertEquals(0, calculateReserveProjection(0, 0, 0).percent)
        assertEquals(100, calculateReserveProjection(120_000, 100_000, 5_000).percent)
        assertEquals(0, calculateReserveProjection(120_000, 100_000, 5_000).remainingMinor)
        assertNull(calculateReserveProjection(20_000, 100_000, 0).monthsToGoal)
    }

    @Test
    fun reserveProjectionRoundsMonthsUp() {
        val result = calculateReserveProjection(balanceMinor = 100, goalMinor = 1_001, monthlyContributionMinor = 300)
        assertEquals(901, result.remainingMinor)
        assertEquals(4, result.monthsToGoal)
    }

    @Test
    fun moneyParsingUsesCentsAndRejectsInvalidOrNegativeAmounts() {
        assertEquals(123_456L, parseMoneyToMinorUnits("1.234,56"))
        assertEquals(0L, parseMoneyToMinorUnits(""))
        assertNull(parseMoneyToMinorUnits("-1,00"))
        assertNull(parseMoneyToMinorUnits("12,345"))
        assertNull(parseMoneyToMinorUnits("abc"))
    }
}
