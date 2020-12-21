package com.inspirecoding.supershopper.di

import com.inspirecoding.supershopper.repository.local.ShopperRepository
import com.inspirecoding.supershopper.repository.local.ShopperRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton


@ExperimentalCoroutinesApi
@InstallIn(ApplicationComponent::class)
@Module
abstract class ShopperRepositoryModule {
    @Binds
    @Singleton
    abstract fun providesShopperRepository (
        shopperRepositoryImpl: ShopperRepositoryImpl
    ): ShopperRepository
}