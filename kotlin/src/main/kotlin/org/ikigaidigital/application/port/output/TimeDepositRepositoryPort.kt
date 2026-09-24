package org.ikigaidigital.application.port.output

import org.ikigaidigital.domain.TimeDeposit
import org.ikigaidigital.domain.TimeDepositRecord

interface TimeDepositRepositoryPort {
    fun findAll(): List<TimeDepositRecord>
    fun saveAll(timeDeposits: List<TimeDeposit>)
}
