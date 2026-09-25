package org.ikigaidigital.application.port.input

import org.ikigaidigital.application.models.TimeDepositRecord

fun interface GetAllTimeDepositsUseCase {
    fun getAllTimeDeposits(): List<TimeDepositRecord>
}
