package com.inspirecoding.supershopper.repository.local

import androidx.room.*
import com.inspirecoding.supershopper.data.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopperDao {

    @Query("SELECT * FROM category ORDER BY position DESC")
    fun getCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

}