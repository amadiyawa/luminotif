package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_users.domain.model.OldUser
import com.amadiyawa.feature_users.domain.repository.OldUserRepository

internal class GetUserListUseCase(
    private val oldUserRepository: OldUserRepository,
) {
    suspend operator fun invoke(page: Int = 1, results: Int = 10): OperationResult<List<OldUser>> {
        return oldUserRepository
            .getUsers(page, results)
    }
}