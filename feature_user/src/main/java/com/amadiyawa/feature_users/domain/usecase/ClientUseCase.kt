package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_users.domain.model.Client
import com.amadiyawa.feature_users.domain.repository.UserRepository

// Client Use Cases
class GetAllClientsUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): OperationResult<List<Client>> {
        return repository.getAllClients()
    }
}

class GetClientByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(clientId: String): OperationResult<Client> {
        return repository.getClientById(clientId)
    }
}

class SearchClientsUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(query: String): OperationResult<List<Client>> {
        return repository.searchClients(query)
    }
}

class GetClientsByAreaUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(area: String): OperationResult<List<Client>> {
        return repository.getClientsByArea(area)
    }
}

class CreateClientUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(client: Client): OperationResult<Unit> {
        return repository.createClient(client)
    }
}

class UpdateClientUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(client: Client): OperationResult<Unit> {
        return repository.updateClient(client)
    }
}

class ChangeClientStatusUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(clientId: String, status: UserStatus): OperationResult<Unit> {
        return repository.changeClientStatus(clientId, status)
    }
}