package org.ikigaidigital.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "`withdrawals`")
class WithdrawalEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`timeDepositId`", nullable = false)
    val timeDeposit: TimeDepositEntity,

    @Column(nullable = false)
    val amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val date: LocalDate = LocalDate.now()
) {
    val timeDepositId: Int
        get() = requireNotNull(timeDeposit.id)
}
