package org.ikigaidigital.application.models

import org.ikigaidigital.domain.models.TimeDeposit
import org.ikigaidigital.domain.models.Withdrawal

data class TimeDepositRecord(
    val timeDeposit: TimeDeposit,
    val withdrawals: List<Withdrawal>
)
