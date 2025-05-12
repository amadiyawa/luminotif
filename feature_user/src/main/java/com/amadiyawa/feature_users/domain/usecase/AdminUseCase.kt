package com.amadiyawa.feature_users.domain.usecase

import com.amadiyawa.feature_base.domain.result.OperationResult
import com.amadiyawa.feature_base.domain.util.UserStatus
import com.amadiyawa.feature_users.domain.model.Admin
import com.amadiyawa.feature_users.domain.repository.UserRepository

// Admin Use Cases
class GetAllAdminsUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): OperationResult<List<Admin>> {
        return repository.getAllAdmins()
    }
}

class GetAdminByIdUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(adminId: String): OperationResult<Admin> {
        return repository.getAdminById(adminId)
    }
}

class CreateAdminUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(admin: Admin): OperationResult<Unit> {
        return repository.createAdmin(admin)
    }
}

class UpdateAdminUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(admin: Admin): OperationResult<Unit> {
        return repository.updateAdmin(admin)
    }
}

class ChangeAdminStatusUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(adminId: String, status: UserStatus): OperationResult<Unit> {
        return repository.changeAdminStatus(adminId, status)
    }
}
