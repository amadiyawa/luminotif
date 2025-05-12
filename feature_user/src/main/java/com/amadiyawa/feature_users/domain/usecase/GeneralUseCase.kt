package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.model.User
import com.amadiyawa.feature_users.domain.repository.UserRepository
import com.amadiyawa.feature_users.domain.repository.UserStatistics

// General Use Cases
class GetUserByEmailUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String): OperationResult<User> {
        return repository.getUserByEmail(email)
    }
}

class IsEmailTakenUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String): OperationResult<Boolean> {
        return repository.isEmailTaken(email)
    }
}

class IsPhoneTakenUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(phoneNumber: String): OperationResult<Boolean> {
        return repository.isPhoneTaken(phoneNumber)
    }
}

class GetUserStatisticsUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): OperationResult<UserStatistics> {
        return repository.getUserStatistics()
    }
}