package org.ikigaidigital.adapter.output.persistence

import org.ikigaidigital.application.port.output.TimeDepositRepositoryPort
import org.ikigaidigital.domain.TimeDeposit
import org.ikigaidigital.domain.TimeDepositRecord
import org.ikigaidigital.domain.Withdrawal
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TimeDepositPersistenceAdapter(
    private val timeDeposits: TimeDepositJpaRepository
) : TimeDepositRepositoryPort {

    override fun findAll(): List<TimeDepositRecord> =
        timeDeposits.findAllEagerly().map { it.toRecord() }

    override fun saveAll(timeDeposits: List<TimeDeposit>) {
        val byId = timeDeposits.associateBy { it.id }
        val entities = this.timeDeposits.findAllById(byId.keys).map { entity ->
            val updated = byId.getValue(requireNotNull(entity.id))
            TimeDepositEntity(
                id = entity.id,
                planType = entity.planType,
                days = entity.days,
                balance = BigDecimal.valueOf(updated.balance),
                withdrawals = entity.withdrawals
            )
        }
        this.timeDeposits.saveAll(entities)
    }

    private fun TimeDepositEntity.toRecord() = TimeDepositRecord(
        timeDeposit = TimeDeposit(
            id = requireNotNull(id),
            planType = planType,
            balance = balance.toDouble(),
            days = days
        ),
        withdrawals = withdrawals.map { Withdrawal(requireNotNull(it.id), it.timeDepositId, it.amount, it.date) }
    )
}
