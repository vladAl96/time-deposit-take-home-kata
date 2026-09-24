package org.ikigaidigital.domain

import java.math.BigDecimal
import java.math.RoundingMode

class TimeDepositCalculator {
    fun updateBalance(xs: List<TimeDeposit>) {
        for (i in xs.indices) {
            val interest = calculateInterest(xs[i])
            val a2d = BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)
            xs[i].balance += a2d.toDouble()
        }
    }

    fun calculateInterest(deposit: TimeDeposit): Double {
        var interest = 0.0
        val plan = PlanType.valueFrom(deposit.planType)

        if(deposit.days > plan.onsetDays)
        {
         if(plan.cutOffDays == NO_CUTOFF || deposit.days <= plan.cutOffDays)
         {
          interest = deposit.balance * plan.interest / 12
         }
        }

        return interest
    }
}