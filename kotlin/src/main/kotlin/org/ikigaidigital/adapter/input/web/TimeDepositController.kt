package org.ikigaidigital.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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

    @Operation(
        summary = "Update balances of all time deposits",
        description = "Applies the monthly interest calculation to every time deposit and persists the updated balances."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Balances recalculated and persisted successfully")
        ]
    )
    @PostMapping("/update-balances")
    fun updateBalances() {
        updateTimeDepositBalances.updateAllBalances()
    }

    @Operation(
        summary = "Retrieve all time deposits",
        description = "Returns every time deposit currently stored, including its withdrawals."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Time deposits retrieved successfully",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = TimeDepositResponse::class))
                    )
                ]
            )
        ]
    )
    @GetMapping
    fun getAll(): List<TimeDepositResponse> =
        getAllTimeDeposits.getAllTimeDeposits().map { TimeDepositResponse.from(it) }
}
