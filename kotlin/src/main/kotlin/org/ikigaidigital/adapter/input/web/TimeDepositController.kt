package org.ikigaidigital.adapter.input.web

import org.ikigaidigital.adapter.input.web.dto.TimeDepositResponse
import org.ikigaidigital.application.port.input.GetAllTimeDepositsUseCase
import org.ikigaidigital.application.port.input.UpdateTimeDepositBalancesUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/time-deposits")
class TimeDepositController(
    private val updateTimeDepositBalances: UpdateTimeDepositBalancesUseCase,
    private val getAllTimeDeposits: GetAllTimeDepositsUseCase
) {

    @PostMapping("/update-balances")
    fun updateBalances() {
        updateTimeDepositBalances.updateAllBalances()
    }

    @GetMapping
    fun getAll(): List<TimeDepositResponse> =
        getAllTimeDeposits.getAllTimeDeposits().map { TimeDepositResponse.from(it) }
}
