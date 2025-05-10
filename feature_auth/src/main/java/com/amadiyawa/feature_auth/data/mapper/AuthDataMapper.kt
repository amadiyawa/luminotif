package com.amadiyawa.feature_auth.data.mapper

import com.amadiyawa.feature_auth.data.dto.response.AuthResponse
import com.amadiyawa.feature_auth.data.dto.response.OtpVerificationResponse
import com.amadiyawa.feature_auth.data.dto.response.TokenResponse
import com.amadiyawa.feature_auth.data.dto.response.UserResponse
import com.amadiyawa.feature_auth.data.dto.response.VerificationResponse
import com.amadiyawa.feature_auth.domain.model.AdminData
import com.amadiyawa.feature_auth.domain.model.AdminUser
import com.amadiyawa.feature_auth.domain.model.AgentData
import com.amadiyawa.feature_auth.domain.model.AgentUser
import com.amadiyawa.feature_auth.domain.model.AuthResult
import com.amadiyawa.feature_auth.domain.model.AuthTokens
import com.amadiyawa.feature_auth.domain.model.ClientData
import com.amadiyawa.feature_auth.domain.model.ClientUser
import com.amadiyawa.feature_auth.domain.model.OtpVerificationResult
import com.amadiyawa.feature_auth.domain.model.User
import com.amadiyawa.feature_auth.domain.model.VerificationResult
import com.amadiyawa.feature_auth.domain.util.VerificationType
import com.amadiyawa.feature_base.domain.util.AdminAccessLevel
import com.amadiyawa.feature_base.domain.util.UserRole
import com.amadiyawa.feature_base.domain.util.UserStatus

/**
 * Object responsible for mapping authentication-related data transfer objects (DTOs)
 * to domain models. This ensures a clear separation between the data layer and the
 * domain layer by converting API responses into application-specific models.
 */
internal object AuthDataMapper {

    fun AuthResponse.toDomain(): AuthResult {
        return AuthResult(
            user = user.toDomain(),
            token = tokens.toDomain(),
            metadata = metadata,
        )
    }

    fun UserResponse.toDomain(): User {
        // Create the appropriate user type based on role
        return when (this.role.uppercase()) {
            "CLIENT" -> ClientUser(
                id = id,
                name = fullName,
                email = email,
                phone = phoneNumber ?: "",
                avatarUrl = avatarUrl,
                role = UserRole.CLIENT,
                status = UserStatus.ACTIVE,
                clientData = this.clientData?.let {
                    ClientData(
                        accountNumber = it.accountNumber,
                        meterNumber = it.meterNumber,
                        area = it.area,
                        address = it.address
                    )
                } ?: ClientData(
                    accountNumber = "",
                    meterNumber = "",
                    area = "",
                    address = ""
                )
            )

            "AGENT" -> AgentUser(
                id = id,
                name = fullName,
                email = email,
                phone = phoneNumber ?: "",
                avatarUrl = avatarUrl,
                role = UserRole.AGENT,
                status = UserStatus.ACTIVE,
                agentData = this.agentData?.let {
                    AgentData(
                        employeeId = it.employeeId,
                        territories = it.territories
                    )
                } ?: AgentData(
                    employeeId = "",
                    territories = emptyList()
                )
            )

            "ADMIN" -> AdminUser(
                id = id,
                name = fullName,
                email = email,
                phone = phoneNumber ?: "",
                avatarUrl = avatarUrl,
                role = UserRole.ADMIN,
                status = UserStatus.ACTIVE,
                adminData = this.adminData?.let {
                    AdminData(
                        accessLevel = when (it.accessLevel.uppercase()) {
                            "SUPER_ADMIN" -> AdminAccessLevel.SUPER_ADMIN
                            "MANAGER" -> AdminAccessLevel.MANAGER
                            else -> AdminAccessLevel.BASIC
                        }
                    )
                } ?: AdminData(
                    accessLevel = AdminAccessLevel.BASIC
                )
            )

            // Default to a basic user if role doesn't match
            else -> User(
                id = id,
                name = fullName,
                email = email,
                phone = phoneNumber ?: "",
                avatarUrl = avatarUrl,
                role = UserRole.CLIENT,
                status = UserStatus.ACTIVE
            )
        }
    }

    fun TokenResponse.toDomain(): AuthTokens {
        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
            issuedAt = issuedAt,
            tokenType = tokenType
        )
    }

    fun VerificationResponse.toDomain(): VerificationResult {
        return VerificationResult(
            verificationId = verificationId,
            recipient = recipient,
            expiresIn = expiresIn,
            type = when {
                recipient.contains('@') -> VerificationType.EMAIL
                recipient.startsWith('+') -> VerificationType.SMS
                else -> VerificationType.UNKNOWN
            }
        )
    }

    fun OtpVerificationResponse.toDomain(): OtpVerificationResult {
        return OtpVerificationResult(
            success = success,
            purpose = purpose,
            message = message,
            authResponse = authResponse,
            resetToken = resetToken
        )
    }
}