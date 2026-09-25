package org.ikigaidigital.application.port.output

import org.ikigaidigital.domain.models.TimeDeposit
import org.ikigaidigital.application.models.TimeDepositRecord

interface TimeDepositRepositoryPort {
    fun findAll(): List<TimeDepositRecord>
    fun saveAll(timeDeposits: List<TimeDeposit>)
}
