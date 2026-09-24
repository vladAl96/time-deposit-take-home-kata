package org.ikigaidigital.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TimeDepositJpaRepository : JpaRepository<TimeDepositEntity, Int>{
    @Query(
        """
        SELECT DISTINCT td
        FROM TimeDepositEntity td
        LEFT JOIN FETCH td.withdrawals
    """
    )
    fun findAllEagerly(): List<TimeDepositEntity>
}
