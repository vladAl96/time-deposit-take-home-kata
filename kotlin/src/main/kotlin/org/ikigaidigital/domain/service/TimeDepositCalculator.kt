package org.ikigaidigital.domain.service

import org.ikigaidigital.domain.models.PlanType
import org.ikigaidigital.domain.models.TimeDeposit
import java.math.BigDecimal
import java.math.RoundingMode

class TimeDepositCalculator {
    companion object {
        private fun calculateInterest(deposit: TimeDeposit): Double {
            var interest = 0.0
            val plan = PlanType.valueFrom(deposit.planType)

            if(deposit.days > plan.onsetDays)
            {
                if(withinCutoff(deposit, plan))
                {
                    interest = deposit.balance * plan.interestRate / 12
                }
            }

            return interest
        }

        private fun withinCutoff(deposit: TimeDeposit, plan: PlanType): Boolean =
            plan.cutOffDays?.let { deposit.days <= it } ?: true
    }

    fun updateBalance(deposits: List<TimeDeposit>) {
        for (deposit in deposits) {
            val interest = calculateInterest(deposit)
            val decimalInterest = BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)
            deposit.balance += decimalInterest.toDouble()
        }
    }
}