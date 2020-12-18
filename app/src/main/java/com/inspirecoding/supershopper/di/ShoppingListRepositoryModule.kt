package com.inspirecoding.supershopper.di

import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepository
import com.inspirecoding.supershopper.repository.shoppinglist.ShoppingListRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
abstract class ShoppingListRepositoryModule {

    @Binds
    @Singleton
    abstract fun providesShoppingListRepository(
        shoppingListRepositoryImpl: ShoppingListRepositoryImpl
    ) : ShoppingListRepository

}