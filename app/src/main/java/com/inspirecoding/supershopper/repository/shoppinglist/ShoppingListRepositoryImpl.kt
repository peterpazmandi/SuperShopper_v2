package com.inspirecoding.supershopper.repository.shoppinglist

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.data.ListItem
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.ShoppingList
import com.inspirecoding.supershopper.data.User
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

    // COLLECTIONS
    private val shoppingListsCollectionReference = FirebaseFirestore.getInstance().collection(SHOPPINGLIST_COLLECTION_NAME)

    override suspend fun getCurrentUserShoppingListsRealTime(
        currentUser: User, coroutineScope: CoroutineScope
    ) : Flow<Resource<MutableList<ShoppingList>>> = callbackFlow {

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

    override suspend fun getShoppingListRealTime(
        shoppingListId: String, coroutineScope: CoroutineScope
    ): Flow<Resource<ShoppingList>> = callbackFlow {

        offer(Resource.Loading(true))

        val subscription = shoppingListsCollectionReference.document(shoppingListId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                coroutineScope.launch {
                    val shoppingList = documentSnapshot?.let { it.toObject(ShoppingList::class.java) }
                    shoppingList?.let {
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

//        emit(Resource.Loading(true))
        println("DueDate -> $shoppingListId")
        println("DueDate -> $dueDate")
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
}