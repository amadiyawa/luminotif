package com.amadiyawa.feature_auth.domain.model

import com.amadiyawa.feature_base.domain.util.AdminAccessLevel
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.domain.util.UserStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

@Serializable
// Base User class
open class User(
    open val id: String,
    open val name: String,
    open val email: String,
    open val phone: String,
    open val avatarUrl: String? = null,
    open val role: UserRole,
    open val status: UserStatus
)

// User types
data class ClientUser(
    override val id: String,
    override val name: String,
    override val email: String,
    override val phone: String,
    override val avatarUrl: String? = null,
    override val role: UserRole,
    override val status: UserStatus,
    val clientData: ClientData
) : User(id, name, email, phone, avatarUrl, role, status)

data class AgentUser(
    override val id: String,
    override val name: String,
    override val email: String,
    override val phone: String,
    override val avatarUrl: String? = null,
    override val role: UserRole,
    override val status: UserStatus,
    val agentData: AgentData
) : User(id, name, email, phone, avatarUrl, role, status)

data class AdminUser(
    override val id: String,
    override val name: String,
    override val email: String,
    override val phone: String,
    override val avatarUrl: String? = null,
    override val role: UserRole,
    override val status: UserStatus,
    val adminData: AdminData
) : User(id, name, email, phone, avatarUrl, role, status)

// Data classes for user-specific information
data class ClientData(
    val accountNumber: String,
    val meterNumber: String,
    val area: String,
    val address: String
)

data class AgentData(
    val employeeId: String,
    val territories: List<String>
)

data class AdminData(
    val accessLevel: AdminAccessLevel
)

fun User.toJson(): String {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    return json.encodeToString(User.serializer(), this)
}

fun String.toSignIn(): User? {
    return try {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        json.decodeFromString(User.serializer(), this)
    } catch (e: Exception) {
        Timber.e(e, "Error parsing SignIn from JSON")
        null
    }
}