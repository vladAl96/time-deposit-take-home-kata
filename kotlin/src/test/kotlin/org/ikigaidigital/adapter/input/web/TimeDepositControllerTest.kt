package org.ikigaidigital.adapter.input.web

import org.ikigaidigital.application.port.input.GetAllTimeDepositsUseCase
import org.ikigaidigital.application.port.input.UpdateTimeDepositBalancesUseCase
import org.ikigaidigital.domain.TimeDeposit
import org.ikigaidigital.domain.TimeDepositRecord
import org.ikigaidigital.domain.Withdrawal
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate

/** Web-layer slice test: the use cases are mocked, only routing/serialization is under test. */
@WebMvcTest(TimeDepositController::class)
class TimeDepositControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var updateTimeDepositBalances: UpdateTimeDepositBalancesUseCase

    @MockBean
    lateinit var getAllTimeDeposits: GetAllTimeDepositsUseCase

    @Test
    fun `POST update-balances delegates to the use case and returns 200`() {
        mockMvc.post("/api/time-deposits/update-balances")
            .andExpect { status { isOk() } }

        verify(updateTimeDepositBalances).updateAllBalances()
    }

    @Test
    fun `GET returns time deposits shaped as id, planType, balance, days, withdrawals`() {
        val record = TimeDepositRecord(
            timeDeposit = TimeDeposit(id = 1, planType = "basic", balance = 1000.83, days = 31),
            withdrawals = listOf(
                Withdrawal(id = 1, timeDepositId = 1, amount = BigDecimal("50.00"), date = LocalDate.of(2026, 1, 15))
            )
        )
        given(getAllTimeDeposits.getAllTimeDeposits()).willReturn(listOf(record))

        mockMvc.get("/api/time-deposits")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].planType") { value("basic") }
                jsonPath("$[0].balance") { value(1000.83) }
                jsonPath("$[0].days") { value(31) }
                jsonPath("$[0].withdrawals[0].id") { value(1) }
                jsonPath("$[0].withdrawals[0].amount") { value(50.00) }
                jsonPath("$[0].withdrawals[0].date") { value("2026-01-15") }
            }
    }
}
