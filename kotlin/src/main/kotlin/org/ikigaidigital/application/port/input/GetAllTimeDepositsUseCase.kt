package org.ikigaidigital.application.port.input

import org.ikigaidigital.domain.TimeDepositRecord

fun interface GetAllTimeDepositsUseCase {
    fun getAllTimeDeposits(): List<TimeDepositRecord>
}
