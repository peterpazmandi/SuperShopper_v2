package com.inspirecoding.supershopper.repository.shoppinglist

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor() : ShoppingListRepository {

    // CONST
    private val TAG = this.javaClass.simpleName
    private val SHOPPINGLIST_COLLECTION_NAME = "shoppingList"
    private val FRIENDSSHAREDWITH = "friendsSharedWith"
    private val DUEDATE = "dueDate"

    // COLLECTIONS
    private val shoppingListsCollectionReference = FirebaseFirestore.getInstance().collection(SHOPPINGLIST_COLLECTION_NAME)

    @ExperimentalCoroutinesApi
    override suspend fun getCurrentUserShoppingListsRealTime(currentUser: User, coroutineScope: CoroutineScope) : Flow<Resource<MutableList<ShoppingList>>> = callbackFlow {

        offer(Resource.Loading(true))

        val subscription = shoppingListsCollectionReference
            .whereArrayContains(FRIENDSSHAREDWITH, currentUser.id)
            .orderBy(DUEDATE, Query.Direction.DESCENDING)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                coroutineScope.launch {

                    val shoppingLists = querySnapshot?.documents?.mapNotNull {
                        it.toObject(ShoppingList::class.java)
                    }?.toMutableList()

                    shoppingLists?.let {
                        Log.d(TAG, "$shoppingLists")
                        offer(Resource.Success(it))
                    }
                }
            }

        awaitClose { subscription.remove() }

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)






}