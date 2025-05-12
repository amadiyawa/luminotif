package com.amadiyawa.feature_auth.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID
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

fun AuthResponse.Companion.random(userRole: String = "AGENT"): AuthResponse {
    // Cameroon-specific data
    val cameroonNames = listOf(
        Pair("Erwin", "Smith"),
        Pair("Levi", "Ackerman"),
        Pair("Itachi", "Uchiwa"),
        Pair("Naruto", "Uzumaki"),
        Pair("Amadou", "Iyawa"),
        Pair("Obito", "Uchiha"),
        Pair("Shikamaru", "Nara"),
        Pair("Florence", "Biloa"),
        Pair("Michel", "Tchinda"),
        Pair("Bernadette", "Mbida")
    )

    val cameroonCities = listOf(
        "Yaoundé", "Douala", "Garoua", "Bamenda", "Maroua",
        "Bafoussam", "Ngaoundéré", "Bertoua", "Edéa", "Kribi"
    )

    val cameroonAreas = listOf(
        "Bastos", "Essos", "Kpokolota", "Akwa", "Bonapriso",
        "Mokolo", "Madagascar", "Omnisport", "Yademe", "Mvan",
        "Nkoldongo", "Mokolo safari", "Biyem-Assi", "Nsimeyong", "Cité Verte"
    )

    val randomName = cameroonNames.random()
    val fullName = "${randomName.first} ${randomName.second}"
    val baseEmail = "${randomName.first.lowercase()}.${randomName.second.lowercase()}"
    val randomCity = cameroonCities.random()
    val randomArea = cameroonAreas.random()
    val randomId = UUID.randomUUID().toString()
    val avatar = "https://avatars.githubusercontent.com/u/31802381?v=4"

    // Generate random user data based on role
    val user = when (userRole.uppercase()) {
        "CLIENT" -> {
            UserResponse(
                id = randomId,
                fullName = fullName,
                email = "$baseEmail@gmail.com",
                phoneNumber = "+237${(600000000..699999999).random()}",
                avatarUrl = avatar,
                role = "CLIENT",
                clientData = ClientData(
                    accountNumber = "AC${(1000000..9999999).random()}",
                    meterNumber = "MT${(100000..999999).random()}",
                    area = randomArea,
                    address = "${Random.nextInt(1, 200)} $randomArea, $randomCity"
                ),
                status = listOf("ACTIVE", "PENDING_VERIFICATION").random(),
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(30, 365)).toString()
            )
        }
        "AGENT" -> {
            val territoryCount = Random.nextInt(2, 5)
            val territories = cameroonAreas.shuffled().take(territoryCount)

            UserResponse(
                id = randomId,
                fullName = fullName,
                email = "$baseEmail@eneo.cm",
                phoneNumber = "+237${(670000000..699999999).random()}",
                avatarUrl = avatar,
                role = "AGENT",
                agentData = AgentData(
                    employeeId = "EMP${(1000..9999).random()}",
                    territories = territories
                ),
                status = "ACTIVE",
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(180, 730)).toString()
            )
        }
        "ADMIN" -> {
            val adminRole = when (Random.nextInt(1, 6)) {
                1 -> "SUPER_ADMIN"
                2, 3 -> "BILLING_ADMIN"
                else -> "SERVICE_ADMIN"
            }

            UserResponse(
                id = randomId,
                fullName = fullName,
                email = "$baseEmail@admin.eneo.cm",
                phoneNumber = "+237${(690000000..699999999).random()}",
                avatarUrl = avatar,
                role = "ADMIN",
                adminData = AdminData(
                    accessLevel = adminRole
                ),
                status = "ACTIVE",
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(365, 1095)).toString()
            )
        }
        else -> {
            // Default to CLIENT
            UserResponse(
                id = randomId,
                fullName = fullName,
                email = "$baseEmail@gmail.com",
                phoneNumber = "+237${(600000000..699999999).random()}",
                avatarUrl = avatar,
                role = "CLIENT",
                clientData = ClientData(
                    accountNumber = "AC${(1000000..9999999).random()}",
                    meterNumber = "MT${(100000..999999).random()}",
                    area = randomArea,
                    address = "${Random.nextInt(1, 200)} $randomArea, $randomCity"
                ),
                status = "ACTIVE",
                createdAt = LocalDateTime.now().minusDays(Random.nextLong(30, 365)).toString()
            )
        }
    }

    return AuthResponse(
        user = user,
        tokens = TokenResponse(
            accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${UUID.randomUUID().toString().replace("-", "")}",
            refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${UUID.randomUUID().toString().replace("-", "")}",
            expiresIn = Random.nextLong(3600, 7200)
        ),
        metadata = mapOf(
            "provider" to "email", // Since we're using Eneo domain emails
            "is_first_login" to Random.nextBoolean().toString(),
            "last_login" to LocalDateTime.now().minusDays(Random.nextLong(0, 7)).toString(),
            "device_type" to listOf("mobile", "web", "tablet").random()
        )
    )
}