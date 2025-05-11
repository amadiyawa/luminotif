package com.amadiyawa.feature_requests.domain.model

import java.time.LocalDateTime

data class ServiceRequest(
    val id: String,
    val clientId: String,
    val title: String,
    val description: String,
    val category: RequestCategory,
    val priority: RequestPriority,
    val status: RequestStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val assignedAgentId: String?
)

enum class RequestCategory {
    CONNECTION_ISSUE,
    BILLING_QUERY,
    METER_PROBLEM,
    POWER_OUTAGE,
    OTHER
}

enum class RequestPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class RequestStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}