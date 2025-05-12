package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_users.domain.model.Agent
import com.amadiyawa.feature_users.domain.repository.UserRepository

// Agent Use Cases
class GetAllAgentsUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): OperationResult<List<Agent>> {
        return repository.getAllAgents()
    }
}

class GetAgentByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(agentId: String): OperationResult<Agent> {
        return repository.getAgentById(agentId)
    }
}

class CreateAgentUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(agent: Agent): OperationResult<Unit> {
        return repository.createAgent(agent)
    }
}

class UpdateAgentUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(agent: Agent): OperationResult<Unit> {
        return repository.updateAgent(agent)
    }
}

class ChangeAgentStatusUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(agentId: String, status: UserStatus): OperationResult<Unit> {
        return repository.changeAgentStatus(agentId, status)
    }
}

class GetAgentsByTerritoryUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(territory: String): OperationResult<List<Agent>> {
        return repository.getAgentsByTerritory(territory)
    }
}
