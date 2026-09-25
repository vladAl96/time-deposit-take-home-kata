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
import org.springframework.web.bind.annotation.PatchMapping
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
        description = "Applies the monthly interest calculation to every time deposit and persists the updated balances. " +
                "This method is not idempotent and will apply interest calculation on every call."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Balances recalculated and persisted successfully")
        ]
    )
    // PATCH: this recalculates and persists balances in place synchronously, which fits
    // the kata's current scale (a handful of rows, near-instant round trip). If the table
    // grows large enough for this to become a real latency concern, the natural evolution
    // is to make it async (202 Accepted + a background job), but that requires a way for
    // clients to observe completion (a status/polling endpoint) and concurrency control
    // around the read-modify-write of balances (e.g. optimistic locking via a version
    // column, or a DB-level lock) to avoid lost updates if two runs overlap — neither of
    // which the current two-endpoint scope calls for.
    @PatchMapping("/update-balances")
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
