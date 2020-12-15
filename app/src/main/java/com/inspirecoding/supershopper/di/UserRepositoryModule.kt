package com.inspirecoding.supershopper.di

import com.inspirecoding.supershopper.repository.auth.AuthRepository
import com.inspirecoding.supershopper.repository.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
abstract class UserRepositoryModule {

    @Binds
    @Singleton
    abstract fun providesUserRepository (
        userRepositoryImpl: AuthRepositoryImpl
    ) : AuthRepository

}