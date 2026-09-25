package org.ikigaidigital.adapter.input.web.dto

import org.ikigaidigital.application.models.TimeDepositRecord
import java.math.BigDecimal
import java.time.LocalDate

data class WithdrawalResponse(
    val id: Int,
    val amount: BigDecimal,
    val date: LocalDate
)

data class TimeDepositResponse(
    val id: Int,
    val planType: String,
    val balance: Double,
    val days: Int,
    val withdrawals: List<WithdrawalResponse>
) {
    companion object {
        fun from(record: TimeDepositRecord) = TimeDepositResponse(
            id = record.timeDeposit.id,
            planType = record.timeDeposit.planType,
            balance = record.timeDeposit.balance,
            days = record.timeDeposit.days,
            withdrawals = record.withdrawals.map { WithdrawalResponse(it.id, it.amount, it.date) }
        )
    }
}
