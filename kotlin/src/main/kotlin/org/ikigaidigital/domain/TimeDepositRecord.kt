package org.ikigaidigital.domain

import org.ikigaidigital.TimeDeposit

/**
 * A [TimeDeposit] together with its withdrawals, as needed by the GET
 * endpoint's response shape. `TimeDeposit` itself carries no withdrawals
 * field so the calculator's untouched input type can keep flowing straight
 * into `TimeDepositCalculator.updateBalance`.
 */
data class TimeDepositRecord(
    val timeDeposit: TimeDeposit,
    val withdrawals: List<Withdrawal>
)
