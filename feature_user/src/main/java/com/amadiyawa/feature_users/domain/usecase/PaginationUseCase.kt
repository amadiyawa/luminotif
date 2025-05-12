package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.repository.UserRepository

// Pagination Use Cases
class GetClientsPagedUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int = 20): OperationResult<List<Client>> {
        return repository.getClientsPaged(page, pageSize)
    }
}

class GetAgentsPagedUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int = 20): OperationResult<List<Agent>> {
        return repository.getAgentsPaged(page, pageSize)
    }
}