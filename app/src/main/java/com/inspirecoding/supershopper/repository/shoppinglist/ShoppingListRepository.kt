package com.inspirecoding.supershopper.repository.shoppinglist

import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {

    suspend fun getCurrentUserShoppingListsRealTime(currentUser: User): Flow<Resource<MutableList<ShoppingList>>>

}