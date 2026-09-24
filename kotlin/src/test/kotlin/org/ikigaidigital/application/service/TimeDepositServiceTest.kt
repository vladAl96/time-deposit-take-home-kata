package org.ikigaidigital.application.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.ikigaidigital.application.port.output.TimeDepositRepositoryPort
import org.ikigaidigital.domain.TimeDeposit
import org.ikigaidigital.domain.TimeDepositCalculator
import org.ikigaidigital.domain.TimeDepositRecord
import org.ikigaidigital.domain.Withdrawal
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class TimeDepositServiceTest {

    /** In-memory fake for the outbound port, standing in for the real JPA adapter. */
    private class FakeTimeDepositRepository(
        private val records: List<TimeDepositRecord>
    ) : TimeDepositRepositoryPort {
        var savedBalances: List<TimeDeposit>? = null
            private set

        override fun findAll(): List<TimeDepositRecord> = records

        override fun saveAll(timeDeposits: List<TimeDeposit>) {
            savedBalances = timeDeposits
        }
    }

    private val service = { repository: TimeDepositRepositoryPort ->
        TimeDepositService(repository, TimeDepositCalculator())
    }

    @Test
    fun `updateAllBalances runs the calculator over every deposit and persists the result`() {
        val basic = TimeDeposit(id = 1, planType = "basic", balance = 1000.0, days = 31)
        val premium = TimeDeposit(id = 2, planType = "premium", balance = 1000.0, days = 46)
        val repository = FakeTimeDepositRepository(
            listOf(TimeDepositRecord(basic, emptyList()), TimeDepositRecord(premium, emptyList()))
        )

        service(repository).updateAllBalances()

        assertThat(basic.balance).isCloseTo(1000.83, Offset.offset(0.001))
        assertThat(premium.balance).isCloseTo(1004.17, Offset.offset(0.001))
        assertThat(repository.savedBalances).containsExactly(basic, premium)
    }

    @Test
    fun `getAllTimeDeposits passes through whatever the repository returns, untouched`() {
        val deposit = TimeDeposit(id = 3, planType = "student", balance = 500.0, days = 100)
        val withdrawal = Withdrawal(
            id = 1,
            timeDepositId = 3,
            amount = BigDecimal("50.00"),
            date = LocalDate.of(2026, 1, 1)
        )
        val records = listOf(TimeDepositRecord(deposit, listOf(withdrawal)))
        val repository = FakeTimeDepositRepository(records)

        val result = service(repository).getAllTimeDeposits()

        assertThat(result).isEqualTo(records)
    }
}
