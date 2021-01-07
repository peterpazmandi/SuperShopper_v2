package com.inspirecoding.supershopper.repository.shoppinglist

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.data.*
import com.inspirecoding.supershopper.utils.Status
import com.inspirecoding.supershopper.utils.Status.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ShoppingListRepositoryImpl @Inject constructor() : ShoppingListRepository {

    // CONST
    private val TAG = this.javaClass.simpleName
    private val SHOPPINGLIST_COLLECTION_NAME = "shoppingList"
    private val FRIENDSSHAREDWITH = "friendsSharedWith"
    private val SHOPPINGLISTID = "shoppingListId"
    private val DUEDATE = "dueDate"
    private val LISTOFITEMS = "listOfItems"

    private val LIMIT_10: Long = 10
    private val LIMIT_20: Long = 20

    // COLLECTIONS
    private val shoppingListsCollectionReference =
        FirebaseFirestore.getInstance().collection(SHOPPINGLIST_COLLECTION_NAME)

    private var lastShoppingListResult: DocumentSnapshot? = null
    private var lastShoppingList: ShoppingList? = null






    override suspend fun getCurrentUserShoppingListsRealTime(
        currentUser: User, coroutineScope: CoroutineScope
    ) : Flow<Resource<MutableList<ShoppingList>>> = callbackFlow {
        offer(Resource.Loading(true))

        val query = if(lastShoppingListResult == null) {
            shoppingListsCollectionReference
                .whereArrayContains(FRIENDSSHAREDWITH, currentUser.id)
                .orderBy(DUEDATE, Query.Direction.DESCENDING)
                .limit(LIMIT_10)
        } else {
            shoppingListsCollectionReference
                .whereArrayContains(FRIENDSSHAREDWITH, currentUser.id)
                .orderBy(DUEDATE, Query.Direction.DESCENDING)
                .limit(LIMIT_10)
                .startAfter(lastShoppingListResult as DocumentSnapshot)
        }

        val subscription = query
            .addSnapshotListener{ querySnapshot, _ ->
                coroutineScope.launch {
                    val shoppingLists = querySnapshot?.documents?.mapNotNull {
                        lastShoppingListResult = it as DocumentSnapshot
                        it.toObject(ShoppingList::class.java)
                    }?.toMutableList()
                    println("lastShoppingListResult -> $lastShoppingListResult")
                    shoppingLists?.let {
                        offer(Resource.Success(it))
                    }
                }
            }

        awaitClose {
            subscription.remove()
        }

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getShoppingListRealTime(
        shoppingListId: String, coroutineScope: CoroutineScope
    ): Flow<Resource<ShoppingList>> = callbackFlow {

        offer(Resource.Loading(true))

        val subscription = shoppingListsCollectionReference
            .document(shoppingListId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                coroutineScope.launch {
                    val shoppingList = documentSnapshot?.let { it.toObject(ShoppingList::class.java) }
                    shoppingList?.let {
                        offer(Resource.Success(it))
                    }
                }
            }


        awaitClose {
            subscription.remove()
        }

    }.catch { exception ->
        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateShoppingListItems(
        shoppingListId: String, listOfItems: List<ListItem>
    ) = flow<Resource<Nothing>> {

        shoppingListsCollectionReference
            .document(shoppingListId)
            .update(LISTOFITEMS, listOfItems)
            .await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateShoppingListDueDate(
        shoppingListId: String, dueDate: Date
    ) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        shoppingListsCollectionReference
            .document(shoppingListId)
            .update(DUEDATE, dueDate)
            .await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateShoppingListsSharedWithFriends(
        shoppingListId: String,
        friendsSharedWith: List<String>
    ) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        shoppingListsCollectionReference
            .document(shoppingListId)
            .update(FRIENDSSHAREDWITH, friendsSharedWith)
            .await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun insertShoppingList(shoppingList: ShoppingList) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        shoppingListsCollectionReference
            .document(shoppingList.shoppingListId)
            .set(shoppingList)
            .await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteShoppingList(shoppingList: ShoppingList) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        shoppingListsCollectionReference
            .document(shoppingList.shoppingListId)
            .delete()
            .await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }
    }.flowOn(Dispatchers.IO)

    override fun clearShoppingListResult() {
        lastShoppingListResult = null
    }
}