package com.amadiyawa.feature_auth.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class AuthResponse(
    @SerialName("user")
    val user: UserResponse,

    @SerialName("tokens")
    val tokens: TokenResponse,

    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

fun AuthResponse.Companion.random(userRole: String = "CLIENT"): AuthResponse {
    val randomId = Random.nextInt(1000, 9999).toString()

    // Generate random user data based on role
    val user = when (userRole.uppercase()) {
        "CLIENT" -> UserResponse(
            id = randomId,
            fullName = "Amadou Iyawa",
            email = "client@luminotif.com",
            phoneNumber = "+237699182482",
            avatarUrl = "https://avatars.githubusercontent.com/u/31802381?v=4",
            role = "CLIENT",
            clientData = ClientData(
                accountNumber = "ENEO-${Random.nextInt(10000, 99999)}",
                meterNumber = "MTR-${Random.nextInt(100000, 999999)}",
                area = "Yaoundé Central",
                address = "123 Main Street, Yaoundé"
            )
        )
        "AGENT" -> UserResponse(
            id = randomId,
            fullName = "Issa Tika",
            email = "agent@luminotif.com",
            phoneNumber = "+237612345678",
            avatarUrl = null,
            role = "AGENT",
            agentData = AgentData(
                employeeId = "EMP-${Random.nextInt(1000, 9999)}",
                territories = listOf("Yaoundé Central", "Yaoundé North")
            )
        )
        "ADMIN" -> UserResponse(
            id = randomId,
            fullName = "Admin User",
            email = "admin@luminotif.com",
            phoneNumber = "+237687654321",
            avatarUrl = null,
            role = "ADMIN",
            adminData = AdminData(
                accessLevel = listOf("BASIC", "MANAGER", "SUPER_ADMIN").random()
            )
        )
        else -> UserResponse(
            id = randomId,
            fullName = "Default User",
            email = "user@luminotif.com",
            phoneNumber = "+237600000000",
            avatarUrl = null,
            role = "CLIENT",
            clientData = ClientData(
                accountNumber = "ENEO-${Random.nextInt(10000, 99999)}",
                meterNumber = "MTR-${Random.nextInt(100000, 999999)}",
                area = "Yaoundé Central",
                address = "123 Main Street, Yaoundé"
            )
        )
    }

    return AuthResponse(
        user = user,
        tokens = TokenResponse(
            accessToken = "token_$randomId",
            refreshToken = "refresh_$randomId".takeIf { Random.nextBoolean() },
            expiresIn = Random.nextLong(3600, 7200)
        ),
        metadata = mapOf(
            "provider" to listOf("Google", "Facebook", "Github").random(),
            "is_first_login" to Random.nextBoolean().toString()
        )
    )
}