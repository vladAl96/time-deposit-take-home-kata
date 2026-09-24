package org.ikigaidigital.domain

// Those would normally be fetched from an external data store (or service)
enum class PlanType(val onsetDays : Int, val cutOffDays : Int?, val interestRate : Double) {
    BASIC(30, null, 0.01),
    STUDENT(30, 365, 0.03),
    PREMIUM(45, null, 0.05);

    companion object {
        fun valueFrom(value: String): PlanType =
            values().firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown plan type: $value")
    }
}

