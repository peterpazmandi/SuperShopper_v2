package com.inspirecoding.supershopper.repository.shoppinglist

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor() : ShoppingListRepository {

    // CONST
    private val SHOPPINGLIST_COLLECTION_NAME = "shoppingList"
    private val FRIENDSSHAREDWITH = "friendsSharedWith"
    private val DUEDATE = "dueDate"

    // COLLECTIONS
    private val shoppingListsCollectionReference = FirebaseFirestore.getInstance().collection(SHOPPINGLIST_COLLECTION_NAME)

    override suspend fun getCurrentUserShoppingListsRealTime(currentUser: User) = flow<Resource<MutableList<ShoppingList>>> {

        emit(Resource.Loading(true))

        val list = shoppingListsCollectionReference
            .whereArrayContains(FRIENDSSHAREDWITH, currentUser.id)
            .orderBy(DUEDATE, Query.Direction.DESCENDING)
            .get().await()
        val shoppingLists = list.documents.mapNotNull {
            it.toObject(ShoppingList::class.java)
        }.toMutableList()

        emit(Resource.Success(shoppingLists))

    }.catch {  exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)






}