package org.ikigaidigital.adapter.output.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "`timeDeposits`")
class TimeDepositEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "`planType`", nullable = false)
    val planType: String = "",

    @Column(name = "days", nullable = false)
    val days: Int = 0,

    @Column(name = "balance", nullable = false)
    val balance: BigDecimal = BigDecimal.ZERO,

    @OneToMany(
        mappedBy = "timeDeposit",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val withdrawals: MutableList<WithdrawalEntity> = mutableListOf()
)
