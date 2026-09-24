package org.ikigaidigital

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

private val TOLERANCE = Offset.offset(0.001)

class TimeDepositCalculatorTest {

    private val calculator = TimeDepositCalculator()

    @Test
    fun `no plan earns interest in the first 30 days`() {
        val deposits = listOf(
            TimeDeposit(1, "basic", 1000.0, 30),
            TimeDeposit(2, "student", 1000.0, 30),
            TimeDeposit(3, "premium", 1000.0, 30)
        )

        calculator.updateBalance(deposits)

        deposits.forEach { assertThat(it.balance).isCloseTo(1000.0, TOLERANCE) }
    }

    @Test
    fun `basic plan earns 1 percent monthly interest once past day 30`() {
        val deposit = TimeDeposit(1, "basic", 1000.0, 31)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1000.83, TOLERANCE)
    }

    @Test
    fun `student plan earns 3 percent monthly interest once past day 30`() {
        val deposit = TimeDeposit(1, "student", 1200.0, 31)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1203.00, TOLERANCE)
    }

    @Test
    fun `student plan still earns interest just before the 1 year cutoff`() {
        val deposit = TimeDeposit(1, "student", 1000.0, 365)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1002.50, TOLERANCE)
    }

    @Test
    fun `student plan earns no interest from day 366 onward`() {
        val deposit = TimeDeposit(1, "student", 1000.0, 366)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1000.0, TOLERANCE)
    }

    @Test
    fun `premium plan earns no interest until past day 45, even though it's past day 30`() {
        val deposit = TimeDeposit(1, "premium", 1000.0, 45)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1000.0, TOLERANCE)
    }

    @Test
    fun `premium plan earns 5 percent monthly interest once past day 45`() {
        val deposit = TimeDeposit(1, "premium", 1000.0, 46)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1004.17, TOLERANCE)
    }

    @Test
    fun `interest is rounded half up to 2 decimals`() {
        // 1234.567 * 0.01 / 12 = 1.02880...  -> rounds up to 1.03
        val deposit = TimeDeposit(1, "basic", 1234.567, 31)

        calculator.updateBalance(listOf(deposit))

        assertThat(deposit.balance).isCloseTo(1235.597, TOLERANCE)
    }

    @Test
    fun `updateBalance mutates every deposit in the list independently`() {
        val deposits = listOf(
            TimeDeposit(1, "basic", 1000.0, 31),
            TimeDeposit(2, "student", 1000.0, 31),
            TimeDeposit(3, "premium", 1000.0, 46)
        )

        calculator.updateBalance(deposits)

        assertThat(deposits[0].balance).isCloseTo(1000.83, TOLERANCE)
        assertThat(deposits[1].balance).isCloseTo(1002.50, TOLERANCE)
        assertThat(deposits[2].balance).isCloseTo(1004.17, TOLERANCE)
    }
}
