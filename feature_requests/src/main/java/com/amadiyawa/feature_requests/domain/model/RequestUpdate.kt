package com.amadiyawa.feature_requests.domain.model

import java.time.LocalDateTime

data class RequestUpdate(
    val id: String,
    val requestId: String,
    val agentId: String,
    val comment: String,
    val timestamp: LocalDateTime,
    val newStatus: RequestStatus
)