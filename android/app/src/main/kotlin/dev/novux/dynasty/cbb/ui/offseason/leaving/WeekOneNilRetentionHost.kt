package dev.novux.dynasty.cbb.ui.offseason.leaving

import dev.novux.dynasty.cbb.core.balance.BalanceNilRules
import dev.novux.dynasty.cbb.core.save.LoadedDynasty
import dev.novux.dynasty.cbb.data.nil.NilAnnualPoolRepository
import dev.novux.dynasty.cbb.data.nil.NilBudgetRepository

data class WeekOneNilRetentionHost(
    val loaded: LoadedDynasty,
    val nilRules: BalanceNilRules,
    val nilBudgetRepository: NilBudgetRepository,
    val nilAnnualPoolRepository: NilAnnualPoolRepository,
)