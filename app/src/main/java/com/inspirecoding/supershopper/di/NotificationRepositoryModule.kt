package com.inspirecoding.supershopper.di

import com.inspirecoding.supershopper.notification.NotificationRepository
import com.inspirecoding.supershopper.notification.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
abstract class NotificationRepositoryModule {

    @Binds
    @Singleton
    abstract fun providesNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

}