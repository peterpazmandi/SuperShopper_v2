package com.inspirecoding.supershopper.repository.local

import com.inspirecoding.supershopper.data.Category
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
class ShopperRepositoryImpl @Inject constructor(
    private val shopperDao: ShopperDao
)  : ShopperRepository {

    override fun getCategories() = shopperDao.getCategories()

    override suspend fun insertCategory(category: Category) {
        TODO("Not yet implemented")
    }

    override suspend fun updateCategory(category: Category) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCategory(category: Category) {
        TODO("Not yet implemented")
    }
}