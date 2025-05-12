package com.amadiyawa.feature_users.domain

import com.amadiyawa.feature_users.domain.usecase.ChangeAdminStatusUseCase
import com.amadiyawa.feature_users.domain.usecase.ChangeAgentStatusUseCase
import com.amadiyawa.feature_users.domain.usecase.ChangeClientStatusUseCase
import com.amadiyawa.feature_users.domain.usecase.CreateAdminUseCase
import com.amadiyawa.feature_users.domain.usecase.CreateAgentUseCase
import com.amadiyawa.feature_users.domain.usecase.CreateClientUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAdminByIdUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAgentByIdUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAgentsByTerritoryUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAgentsPagedUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAllAdminsUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAllAgentsUseCase
import com.amadiyawa.feature_users.domain.usecase.GetAllClientsUseCase
import com.amadiyawa.feature_users.domain.usecase.GetClientByIdUseCase
import com.amadiyawa.feature_users.domain.usecase.GetClientsByAreaUseCase
import com.amadiyawa.feature_users.domain.usecase.GetClientsPagedUseCase
import com.amadiyawa.feature_users.domain.usecase.GetUserByEmailUseCase
import com.amadiyawa.feature_users.domain.usecase.GetUserListUseCase
import com.amadiyawa.feature_users.domain.usecase.GetUserStatisticsUseCase
import com.amadiyawa.feature_users.domain.usecase.GetUserUseCase
import com.amadiyawa.feature_users.domain.usecase.IsEmailTakenUseCase
import com.amadiyawa.feature_users.domain.usecase.IsPhoneTakenUseCase
import com.amadiyawa.feature_users.domain.usecase.ObserveAdminUpdatesUseCase
import com.amadiyawa.feature_users.domain.usecase.ObserveAgentUpdatesUseCase
import com.amadiyawa.feature_users.domain.usecase.ObserveClientUpdatesUseCase
import com.amadiyawa.feature_users.domain.usecase.SearchClientsUseCase
import com.amadiyawa.feature_users.domain.usecase.UpdateAdminUseCase
import com.amadiyawa.feature_users.domain.usecase.UpdateAgentUseCase
import com.amadiyawa.feature_users.domain.usecase.UpdateClientUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val domainModule = module {
    singleOf(::GetUserListUseCase)
    singleOf(::GetUserUseCase)

    // Client use cases
    factory { GetAllClientsUseCase(get()) }
    factory { GetClientByIdUseCase(get()) }
    factory { SearchClientsUseCase(get()) }
    factory { GetClientsByAreaUseCase(get()) }
    factory { CreateClientUseCase(get()) }
    factory { UpdateClientUseCase(get()) }
    factory { ChangeClientStatusUseCase(get()) }

    // Agent use cases
    factory { GetAllAgentsUseCase(get()) }
    factory { GetAgentByIdUseCase(get()) }
    factory { CreateAgentUseCase(get()) }
    factory { UpdateAgentUseCase(get()) }
    factory { ChangeAgentStatusUseCase(get()) }
    factory { GetAgentsByTerritoryUseCase(get()) }

    // Admin use cases
    factory { GetAllAdminsUseCase(get()) }
    factory { GetAdminByIdUseCase(get()) }
    factory { CreateAdminUseCase(get()) }
    factory { UpdateAdminUseCase(get()) }
    factory { ChangeAdminStatusUseCase(get()) }

    // General use cases
    factory { GetUserByEmailUseCase(get()) }
    factory { IsEmailTakenUseCase(get()) }
    factory { IsPhoneTakenUseCase(get()) }
    factory { GetUserStatisticsUseCase(get()) }

    // Pagination use cases
    factory { GetClientsPagedUseCase(get()) }
    factory { GetAgentsPagedUseCase(get()) }

    // Observable use cases
    factory { ObserveClientUpdatesUseCase(get()) }
    factory { ObserveAgentUpdatesUseCase(get()) }
    factory { ObserveAdminUpdatesUseCase(get()) }
}