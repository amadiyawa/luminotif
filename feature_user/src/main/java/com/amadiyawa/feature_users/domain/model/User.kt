package com.amadiyawa.feature_users.domain.model

import com.amadiyawa.feature_base.domain.util.UserStatus
import java.time.LocalDateTime

// Base User class
sealed class User(
    open val id: String,
    open val fullName: String,
    open val email: String,
    open val phoneNumber: String,
    open val createdAt: LocalDateTime,
    open val status: UserStatus,
    open val avatarUrl: String? = null
)

// Client model
data class Client(
    override val id: String,
    override val fullName: String,
    override val email: String,
    override val phoneNumber: String,
    override val createdAt: LocalDateTime,
    override val status: UserStatus,
    override val avatarUrl: String? = null,
    val address: String,
    val meterNumber: String,
    val accountNumber: String,
    val area: String
) : User(id, fullName, email, phoneNumber, createdAt, status, avatarUrl)

// Agent model
data class Agent(
    override val id: String,
    override val fullName: String,
    override val email: String,
    override val phoneNumber: String,
    override val createdAt: LocalDateTime,
    override val status: UserStatus,
    override val avatarUrl: String? = null,
    val employeeId: String,
    val territories: List<String> // Changed from territory to territories to match AuthResponse
) : User(id, fullName, email, phoneNumber, createdAt, status, avatarUrl)

// Admin model
data class Admin(
    override val id: String,
    override val fullName: String,
    override val email: String,
    override val phoneNumber: String,
    override val createdAt: LocalDateTime,
    override val status: UserStatus,
    override val avatarUrl: String? = null,
    val accessLevel: AdminRole // Renamed from role to accessLevel to match AuthResponse
) : User(id, fullName, email, phoneNumber, createdAt, status, avatarUrl)

// Admin Role enum
enum class AdminRole {
    SUPER_ADMIN,
    BILLING_ADMIN,
    SERVICE_ADMIN
}

// Extension functions for conversions if needed
fun User.isActive(): Boolean = status == UserStatus.ACTIVE

fun Agent.canAccessArea(area: String): Boolean = territories.contains(area)

fun Admin.isSuperAdmin(): Boolean = accessLevel == AdminRole.SUPER_ADMIN