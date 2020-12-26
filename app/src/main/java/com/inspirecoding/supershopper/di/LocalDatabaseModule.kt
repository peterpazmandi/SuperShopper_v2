package com.inspirecoding.supershopper.di

import android.app.Application
import androidx.room.Room
import com.inspirecoding.supershopper.repository.local.ShopperDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object LocalDatabaseModule {

    private const val SHOPPER_DATABASE = "shopper_database"

    @Provides
    @Singleton
    fun providesDatabase(
        application: Application,
        callback: ShopperDatabase.Callback
    ) = Room.databaseBuilder(application, ShopperDatabase::class.java, SHOPPER_DATABASE)
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun providesShopperDao(database: ShopperDatabase) = database.shopperDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())

}


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope