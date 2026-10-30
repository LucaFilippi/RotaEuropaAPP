package com.lucafilippi.rotaeuropa.domain

import java.math.BigDecimal
import java.math.RoundingMode

enum class TransactionKind(val label: String, val sign: Int) {
    CONTRIBUTION("Aporte", 1), WITHDRAWAL("Retirada", -1)
}

data class FinancialTransactionItem(
    val id: Long = 0,
    val amountMinor: Long,
    val currency: String,
    val kind: TransactionKind,
    val occurredAtEpochDay: Long,
    val note: String = "",
)

data class BudgetItem(
    val id: Long = 0,
    val name: String,
    val category: String,
    val amountMinor: Long,
    val currency: String,
    val recurring: Boolean,
    val note: String = "",
)

data class ExchangeRateItem(
    val id: Long = 0,
    val brlPerEurMinor: Long,
    val quotedAtEpochDay: Long,
    val source: String,
    val note: String = "",
)

data class FinancialPlan(
    val amountNeededMonthlyMinor: Long,
    val projectedBalanceMinor: Long,
    val monthsRemaining: Long,
    val feasible: Boolean,
)

fun calculateFinancialPlan(
    balanceMinor: Long,
    goalMinor: Long,
    monthlyContributionMinor: Long,
    targetDateEpochDay: Long?,
    todayEpochDay: Long,
    futureCostsMinor: Long = 0,
): FinancialPlan? {
    if (targetDateEpochDay == null) return null
    val months = ((targetDateEpochDay - todayEpochDay).coerceAtLeast(0) / 30.4375).toLong()
    val balance = balanceMinor.coerceAtLeast(0)
    val goal = goalMinor.coerceAtLeast(0)
    val costs = futureCostsMinor.coerceAtLeast(0)
    val maxLong = BigDecimal.valueOf(Long.MAX_VALUE)
    val netGoal = BigDecimal.valueOf(goal).add(BigDecimal.valueOf(costs)).min(maxLong).toLong()
    val missing = (netGoal - balance).coerceAtLeast(0)
    val requiredMonthly = if (months == 0L) missing else BigDecimal.valueOf(missing)
        .divide(BigDecimal.valueOf(months), 0, RoundingMode.CEILING).toLong()
    val projected = BigDecimal.valueOf(balance)
        .add(BigDecimal.valueOf(monthlyContributionMinor.coerceAtLeast(0)).multiply(BigDecimal.valueOf(months)))
        .subtract(BigDecimal.valueOf(costs)).max(BigDecimal.ZERO).min(maxLong).toLong()
    return FinancialPlan(requiredMonthly, projected, months, projected >= goal)
}

fun convertBrlToEurMinor(brlMinor: Long, brlPerEurMinor: Long): Long? {
    if (brlMinor < 0 || brlPerEurMinor <= 0) return null
    return runCatching {
        BigDecimal.valueOf(brlMinor).multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(brlPerEurMinor), 0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()
}

fun convertEurToBrlMinor(eurMinor: Long, brlPerEurMinor: Long): Long? {
    if (eurMinor < 0 || brlPerEurMinor <= 0) return null
    return runCatching {
        BigDecimal.valueOf(eurMinor).multiply(BigDecimal.valueOf(brlPerEurMinor))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()
}

val budgetCategories = listOf(
    "Passagem", "Moradia", "Caução", "Alimentação", "Transporte", "Documentação",
    "Traduções e apostilamentos", "Taxas universitárias", "Certificações", "Saúde e seguro",
    "Equipamentos", "Emergências", "Outros",
)

val journeyRecordTypes = listOf(
    "Experiência profissional" to "experience",
    "Experiência em desenvolvimento" to "dev_experience",
    "Projeto de portfólio" to "project",
    "Formação acadêmica" to "education",
    "Idioma" to "language",
    "Plano de estudos" to "study_plan",
    "Certificação" to "certification",
    "Universidade" to "university",
    "Candidatura" to "application",
    "Documento" to "document",
    "Comparação de destino" to "destination",
    "Marco" to "milestone",
    "Lembrete local" to "reminder",
)
