package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_users.domain.repository.OldUserRepository

internal class GetUserUseCase(
    private val oldUserRepository: OldUserRepository,
) {
    suspend operator fun invoke(uuid: String) = oldUserRepository.getUserByUuid(uuid)
}