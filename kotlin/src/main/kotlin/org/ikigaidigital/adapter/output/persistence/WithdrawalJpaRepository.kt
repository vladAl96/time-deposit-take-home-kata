package org.ikigaidigital.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WithdrawalJpaRepository : JpaRepository<WithdrawalEntity, Int>