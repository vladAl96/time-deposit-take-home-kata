package org.ikigaidigital.domain.models

import java.math.BigDecimal
import java.time.LocalDate

data class Withdrawal(
    val id: Int,
    val timeDepositId: Int,
    val amount: BigDecimal,
    val date: LocalDate
)
