package com.amadiyawa.feature_requests.data

import com.amadiyawa.feature_requests.data.repository.FakeServiceRequestRepository
import com.amadiyawa.feature_requests.domain.repository.ServiceRequestRepository
import org.koin.dsl.module

val dataModule = module {
    single<ServiceRequestRepository> { FakeServiceRequestRepository() }
}