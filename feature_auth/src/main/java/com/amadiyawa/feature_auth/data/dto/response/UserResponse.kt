package com.amadiyawa.feature_auth.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("id")
    val id: String,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("email")
    val email: String,

    @SerialName("phoneNumber")
    val phoneNumber: String?,

    @SerialName("avatarUrl")
    val avatarUrl: String?,

    @SerialName("role")
    val role: String,  // "CLIENT", "AGENT", or "ADMIN"

    @SerialName("clientData")
    val clientData: ClientData? = null,

    @SerialName("agentData")
    val agentData: AgentData? = null,

    @SerialName("adminData")
    val adminData: AdminData? = null
)

@Serializable
data class ClientData(
    @SerialName("accountNumber")
    val accountNumber: String,

    @SerialName("meterNumber")
    val meterNumber: String,

    @SerialName("area")
    val area: String,

    @SerialName("address")
    val address: String
)

@Serializable
data class AgentData(
    @SerialName("employeeId")
    val employeeId: String,

    @SerialName("territories")
    val territories: List<String>
)

@Serializable
data class AdminData(
    @SerialName("accessLevel")
    val accessLevel: String  // "BASIC", "MANAGER", or "SUPER_ADMIN"
)

