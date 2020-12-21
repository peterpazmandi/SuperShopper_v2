package com.inspirecoding.supershopper.repository.local

import com.inspirecoding.supershopper.data.Category
import kotlinx.coroutines.flow.Flow

interface ShopperRepository {

    fun getCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)

}