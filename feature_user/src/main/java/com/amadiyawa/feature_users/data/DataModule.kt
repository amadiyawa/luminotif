package com.amadiyawa.feature_users.data

import androidx.room.Room
import com.amadiyawa.feature_users.data.datasource.api.service.UserRetrofitService
import com.amadiyawa.feature_users.data.datasource.database.UserDatabase
import com.amadiyawa.feature_users.data.repository.FakeUserRepository
import com.amadiyawa.feature_users.data.repository.OldUserRepositoryImpl
import com.amadiyawa.feature_users.domain.repository.OldUserRepository
import com.amadiyawa.feature_users.domain.repository.UserRepository
import org.koin.dsl.module
import retrofit2.Retrofit

internal val dataModule = module {
    single<OldUserRepository> { OldUserRepositoryImpl(get(), get()) }

    single { get<Retrofit>().create(UserRetrofitService::class.java) }


    single {
        Room.databaseBuilder(
            get(),
            UserDatabase::class.java,
            "Users.db",
        ).build()
    }

    single { get<UserDatabase>().users() }

    // Repository
    single<UserRepository> {
        FakeUserRepository(
            userSessionManager = get()
        )
    }
}