package com.inspirecoding.supershopper.repository.shoppinglist

import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {

    suspend fun getCurrentUserShoppingListsRealTime(currentUser: User, coroutineScope: CoroutineScope): Flow<Resource<MutableList<ShoppingList>>>
    suspend fun getShoppingListRealTime(shoppingListId: String, coroutineScope: CoroutineScope): Flow<Resource<ShoppingList>>

    suspend fun updateShoppingListItems(shoppingListId: String, listOfItems: List<ListItem>): Flow<Resource<Nothing>>

    suspend fun insertShoppingList(shoppingList: ShoppingList): Flow<Resource<Nothing>>
}