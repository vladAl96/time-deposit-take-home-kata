package org.ikigaidigital.domain

const val NO_CUTOFF = -1
// Those would normally be fetched from an external data store (or service)
enum class PlanType(val onsetDays : Int, val cutOffDays : Int, val interest : Double) {
    BASIC(30, NO_CUTOFF, 0.01),
    STUDENT(30, 365, 0.03),
    PREMIUM(45, NO_CUTOFF, 0.05);

    companion object {
        fun valueFrom(value: String): PlanType =
            values().firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown plan type: $value")
    }
}

