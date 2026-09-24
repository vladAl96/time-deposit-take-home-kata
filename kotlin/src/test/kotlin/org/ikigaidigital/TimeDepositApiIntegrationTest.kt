package org.ikigaidigital

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

private val TOLERANCE = Offset.offset(0.001)

/**
 * End-to-end through a real Postgres container: HTTP -> service ->
 * TimeDepositCalculator -> JPA -> Postgres and back. Requires Docker.
 * Ordered because both tests share one seeded database: the GET assertions
 * must run before the PATCH mutates balances.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TimeDepositApiIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("time_deposit")
            withUsername("time_deposit")
            withPassword("time_deposit")
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    private fun currentDeposits(): List<Map<String, Any>> {
        val response = mockMvc.get("/api/time-deposits").andReturn().response.contentAsString
        return objectMapper.readValue(response)
    }

    @Test
    @Order(1)
    fun `GET returns the seeded time deposits shaped with withdrawals`() {
        val deposits = currentDeposits()

        assertThat(deposits).hasSize(3)
        val basic = deposits.single { it["id"] == 1 }
        assertThat(basic["planType"]).isEqualTo("basic")
        assertThat(basic["withdrawals"] as List<*>).hasSize(1)
        val studentWithNoWithdrawal = deposits.single { it["id"] == 2 }
        assertThat(studentWithNoWithdrawal["withdrawals"] as List<*>).isEmpty()
    }

    @Test
    @Order(2)
    fun `PATCH update-balances runs the calculator and persists the new balances`() {
        mockMvc.patch("/api/time-deposits/update-balances")
            .andExpect { status { isOk() } }

        val deposits = currentDeposits()
        fun balanceOf(id: Int) = (deposits.single { it["id"] == id }["balance"] as Number).toDouble()

        // seed data: (1, basic, 45d, 1000.00) (2, student, 100d, 2000.00) (3, premium, 60d, 5000.00)
        assertThat(balanceOf(1)).isCloseTo(1000.83, TOLERANCE)
        assertThat(balanceOf(2)).isCloseTo(2005.00, TOLERANCE)
        assertThat(balanceOf(3)).isCloseTo(5020.83, TOLERANCE)
    }
}
