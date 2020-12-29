package com.inspirecoding.supershopper.repository.local

import androidx.room.*
import com.inspirecoding.supershopper.data.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopperDao {

    @Query("SELECT * FROM category ORDER BY position ASC")
    fun getCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category ORDER BY position ASC")
    suspend fun getCategoriesSuspend(): List<Category>

    @Query("SELECT * FROM category WHERE id=:id")
    fun getCategoryByIdWithFlow(id: Int): Flow<Category>

    @Query("SELECT * FROM category WHERE id=:id")
    suspend fun getCategoryByIdSuspend(id: Int): Category

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Update
    suspend fun updateCategories(categories: List<Category>)

    @Delete
    suspend fun deleteCategories(categories: List<Category>)

    @Delete
    suspend fun deleteCategory(category: Category)

}