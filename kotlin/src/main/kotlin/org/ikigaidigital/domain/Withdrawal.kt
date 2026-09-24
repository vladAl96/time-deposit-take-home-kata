package org.ikigaidigital.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Domain representation of a withdrawal against a time deposit.
 * Deliberately separate from [TimeDeposit] so the
 * calculator's input type (and its `updateBalance` signature) stays untouched.
 */
data class Withdrawal(
    val id: Int,
    val timeDepositId: Int,
    val amount: BigDecimal,
    val date: LocalDate
)
