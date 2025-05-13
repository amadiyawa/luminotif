package com.amadiyawa.feature_users.domain.repository

import com.amadiyawa.feature_users.domain.model.OldUser
import com.amadiyawa.feature_base.domain.result.OperationResult

internal interface OldUserRepository {
    suspend fun getUsers(page: Int = 1, results: Int = 10): OperationResult<List<OldUser>>
    suspend fun getUserByUuid(uuid: String): OperationResult<OldUser>
}