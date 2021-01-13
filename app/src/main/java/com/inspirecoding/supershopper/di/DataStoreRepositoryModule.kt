package com.inspirecoding.supershopper.di

import com.inspirecoding.supershopper.repository.datastore.DataStoreRepository
import com.inspirecoding.supershopper.repository.datastore.DataStoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
abstract class DataStoreRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): DataStoreRepository

}