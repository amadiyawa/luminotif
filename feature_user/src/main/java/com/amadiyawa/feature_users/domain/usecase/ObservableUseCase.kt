package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_users.domain.model.Admin
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

// Observable Use Cases
class ObserveClientUpdatesUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<Client>> {
        return repository.observeClientUpdates()
    }
}

class ObserveAgentUpdatesUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<Agent>> {
        return repository.observeAgentUpdates()
    }
}

class ObserveAdminUpdatesUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<Admin>> {
        return repository.observeAdminUpdates()
    }
}