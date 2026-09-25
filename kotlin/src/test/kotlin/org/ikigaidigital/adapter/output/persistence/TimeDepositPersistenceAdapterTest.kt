package org.ikigaidigital.adapter.output.persistence

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.domain.models.TimeDeposit
import org.ikigaidigital.application.models.TimeDepositRecord
import org.ikigaidigital.domain.models.Withdrawal
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.math.BigDecimal
import java.time.LocalDate

class TimeDepositPersistenceAdapterTest {

    private val repository = mock(TimeDepositJpaRepository::class.java)
    private val adapter = TimeDepositPersistenceAdapter(repository)

    @Test
    fun `findAll maps entities (and their withdrawals) to domain records`() {
        val timeDepositEntity = TimeDepositEntity(
            id = 1,
            planType = "basic",
            days = 45,
            balance = BigDecimal("1000.00")
        )
        val withdrawalEntity = WithdrawalEntity(
            id = 10,
            timeDeposit = timeDepositEntity,
            amount = BigDecimal("50.00"),
            date = LocalDate.of(2026, 1, 15)
        )
        timeDepositEntity.withdrawals.add(withdrawalEntity)
        given(repository.findAllEagerly()).willReturn(listOf(timeDepositEntity))

        val result = adapter.findAll()

        assertThat(result).containsExactly(
            TimeDepositRecord(
                timeDeposit = TimeDeposit(id = 1, planType = "basic", balance = 1000.0, days = 45),
                withdrawals = listOf(
                    Withdrawal(
                        id = 10,
                        timeDepositId = 1,
                        amount = BigDecimal("50.00"),
                        date = LocalDate.of(2026, 1, 15)
                    )
                )
            )
        )
    }

    @Test
    fun `findAll returns a deposit with no withdrawals as an empty list`() {
        val timeDepositEntity = TimeDepositEntity(id = 2, planType = "student", days = 100, balance = BigDecimal("2000.00"))
        given(repository.findAllEagerly()).willReturn(listOf(timeDepositEntity))

        val result = adapter.findAll()

        assertThat(result.single().withdrawals).isEmpty()
    }

    @Test
    fun `saveAll persists the calculator's new balance while keeping id, planType, days and withdrawals from the stored entity`() {
        val existingEntity = TimeDepositEntity(id = 1, planType = "premium", days = 60, balance = BigDecimal("5000.00"))
        val existingWithdrawal = WithdrawalEntity(
            id = 5,
            timeDeposit = existingEntity,
            amount = BigDecimal("200.00"),
            date = LocalDate.of(2026, 2, 1)
        )
        existingEntity.withdrawals.add(existingWithdrawal)
        given(repository.findAllById(setOf(1))).willReturn(listOf(existingEntity))

        val updated = TimeDeposit(id = 1, planType = "premium", balance = 5020.83, days = 60)
        adapter.saveAll(listOf(updated))

        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<TimeDepositEntity>>
        verify(repository).saveAll(captor.capture())
        val saved = captor.value.single()
        assertThat(saved.id).isEqualTo(1)
        assertThat(saved.planType).isEqualTo("premium")
        assertThat(saved.days).isEqualTo(60)
        assertThat(saved.balance).isEqualByComparingTo("5020.83")
        assertThat(saved.withdrawals).containsExactly(existingWithdrawal)
    }

    @Test
    fun `saveAll only touches entities present in the given list, even if the repository holds more`() {
        val toUpdate = TimeDepositEntity(id = 1, planType = "basic", days = 45, balance = BigDecimal("1000.00"))
        given(repository.findAllById(setOf(1))).willReturn(listOf(toUpdate))

        adapter.saveAll(listOf(TimeDeposit(id = 1, planType = "basic", balance = 1000.83, days = 45)))

        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<TimeDepositEntity>>
        verify(repository).saveAll(captor.capture())
        assertThat(captor.value).hasSize(1)
    }
}
