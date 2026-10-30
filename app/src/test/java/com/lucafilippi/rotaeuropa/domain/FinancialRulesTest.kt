package com.lucafilippi.rotaeuropa.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialRulesTest {
    @Test
    fun convertsOnlyWithPositiveManualRateAndRoundsToMinorUnit() {
        assertEquals(500L, convertBrlToEurMinor(brlMinor = 2_500, brlPerEurMinor = 500))
        assertEquals(2_500L, convertEurToBrlMinor(eurMinor = 500, brlPerEurMinor = 500))
        assertNull(convertBrlToEurMinor(brlMinor = 2_500, brlPerEurMinor = 0))
        assertNull(convertBrlToEurMinor(brlMinor = -1, brlPerEurMinor = 500))
    }

    @Test
    fun financialPlanIncludesOneTimeCostsAndComparesToGoal() {
        val plan = calculateFinancialPlan(
            balanceMinor = 400_000,
            goalMinor = 1_000_000,
            monthlyContributionMinor = 200_000,
            targetDateEpochDay = 180,
            todayEpochDay = 0,
            futureCostsMinor = 100_000,
        )!!
        assertTrue(plan.monthsRemaining > 0)
        assertEquals(700_000L / plan.monthsRemaining + if (700_000L % plan.monthsRemaining == 0L) 0 else 1, plan.amountNeededMonthlyMinor)
        assertTrue(plan.feasible)
    }

    @Test
    fun missingDateDoesNotInventAPlanAndPastDateUsesCurrentBalance() {
        assertNull(calculateFinancialPlan(0, 100, 0, null, 10))
        val plan = calculateFinancialPlan(100, 100, 0, targetDateEpochDay = 9, todayEpochDay = 10)!!
        assertEquals(0L, plan.monthsRemaining)
        assertEquals(0L, plan.amountNeededMonthlyMinor)
        assertTrue(plan.feasible)
    }
}
