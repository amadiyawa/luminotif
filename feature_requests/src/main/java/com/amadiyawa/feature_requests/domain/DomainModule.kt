package com.amadiyawa.feature_requests.domain

import com.amadiyawa.feature_requests.domain.usecase.AssignServiceRequestUseCase
import com.amadiyawa.feature_requests.domain.usecase.CancelServiceRequestUseCase
import com.amadiyawa.feature_requests.domain.usecase.CreateServiceRequestUseCase
import com.amadiyawa.feature_requests.domain.usecase.FilterServiceRequestsUseCase
import com.amadiyawa.feature_requests.domain.usecase.GetServiceRequestByIdUseCase
import com.amadiyawa.feature_requests.domain.usecase.GetServiceRequestUpdatesUseCase
import com.amadiyawa.feature_requests.domain.usecase.GetServiceRequestsUseCase
import com.amadiyawa.feature_requests.domain.usecase.ResolveServiceRequestUseCase
import com.amadiyawa.feature_requests.domain.usecase.UpdateServiceRequestStatusUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CreateServiceRequestUseCase(get()) }
    factory { GetServiceRequestsUseCase(get()) }
    factory { GetServiceRequestByIdUseCase(get()) }
    factory { UpdateServiceRequestStatusUseCase(get()) }
    factory { AssignServiceRequestUseCase(get()) }
    factory { GetServiceRequestUpdatesUseCase(get()) }
    factory { CancelServiceRequestUseCase(get()) }
    factory { ResolveServiceRequestUseCase(get()) }
    factory { FilterServiceRequestsUseCase(get()) }
}