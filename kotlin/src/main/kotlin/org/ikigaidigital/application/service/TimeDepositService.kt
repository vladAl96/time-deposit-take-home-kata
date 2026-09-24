package org.ikigaidigital.application.service

import org.ikigaidigital.application.port.input.GetAllTimeDepositsUseCase
import org.ikigaidigital.application.port.input.UpdateTimeDepositBalancesUseCase
import org.ikigaidigital.application.port.output.TimeDepositRepositoryPort
import org.ikigaidigital.domain.TimeDepositCalculator
import org.ikigaidigital.domain.TimeDepositRecord
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimeDepositService(
    private val repository: TimeDepositRepositoryPort,
    private val calculator: TimeDepositCalculator = TimeDepositCalculator()
) : UpdateTimeDepositBalancesUseCase, GetAllTimeDepositsUseCase {

    @Transactional
    override fun updateAllBalances() {
        val timeDeposits = repository.findAll().map { it.timeDeposit }
        calculator.updateBalance(timeDeposits)
        repository.saveAll(timeDeposits)
    }

    override fun getAllTimeDeposits(): List<TimeDepositRecord> = repository.findAll()
}
