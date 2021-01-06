package com.inspirecoding.supershopper.repository.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Friend
import com.inspirecoding.supershopper.data.FriendRequest
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import com.inspirecoding.supershopper.utils.ObjectFactory.createUserObject
import com.inspirecoding.supershopper.utils.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : UserRepository {

    //CONST
    private val TAG = this.javaClass.simpleName
    private val USER_COLLECTION_NAME = "users"
    private val FRIENDS_COLLECTION_NAME = "friends"
    private val FRIENDSREQUEST_COLLECTION_NAME = "friend_requests"
    private val FIELD_NAME = "name"
    private val RC_SIGN_IN = 1
    private val LIMIT_10: Long = 10
    private val LIMIT_20: Long = 20

    // COLLECTIONS
    private val usersCollectionReference =
        FirebaseFirestore.getInstance().collection(USER_COLLECTION_NAME)
    private val friendsCollection =
        FirebaseFirestore.getInstance().collection(FRIENDS_COLLECTION_NAME)
    private val friendsRequestCollection  =
        FirebaseFirestore.getInstance().collection(FRIENDSREQUEST_COLLECTION_NAME)

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userResource = Channel<Resource<User>>()
    override val userResource = _userResource.receiveAsFlow()

    private var callbackManager: CallbackManager? = null
    private lateinit var googleSingInClient: GoogleSignInClient

    private var lastResult: DocumentSnapshot? = null


    override suspend fun registerUserFromAuthWithEmailAndPassword(
        username: String, email: String, password: String
    ) {
        _userResource.send(Resource.Loading(true))

        try {

            val resultDocumentSnapshot =
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = resultDocumentSnapshot.user

            if (firebaseUser != null) {
                val user = createUserObject(firebaseUser, username)
                createUserInFirestore(user).collect { _user ->
                    _userResource.send(_user)
                }
            } else {
                _userResource.send(Resource.Error(applicationContext.getString(R.string.error_during_registration_please_try_again_later)))
            }
        } catch (exception: Exception) {
            exception.message?.let {
                _userResource.send(Resource.Error(it))
            }
        }
    }


    override suspend fun logInUserFromAuthWithEmailAndPassword(
        email: String, password: String
    ) {
        _userResource.send(Resource.Loading(true))

        try {
            val resultDocumentSnapshot =
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = resultDocumentSnapshot.user

            if (firebaseUser != null) {
                getUserFromFirestore(firebaseUser.uid).collect { _user ->
                    _userResource.send(_user)
                }
            } else {
                _userResource.send(Resource.Error(applicationContext.getString(R.string.error_during_login_please_try_again_later)))
            }
        } catch (exception: Exception) {
            exception.message?.let {
                _userResource.send(Resource.Error(it))
            }
        }
    }

    override suspend fun checkUserLoggedIn(coroutineScope: CoroutineScope) {

        _userResource.send(Resource.Loading(true))

        try {
            coroutineScope.launch {
                val firebaseUser = firebaseAuth.currentUser

                if (firebaseUser != null) {
                    getUserFromFirestore(firebaseUser.uid).collect { _user ->
                        _userResource.send(_user)
                    }
                } else {
                    _userResource.send(Resource.Success(null))
                }
            }
        } catch (exception: Exception) {
            exception.message?.let {
                _userResource.send(Resource.Error(it))
            }
        }
    }


    //Facebook
    override suspend fun signInWithFacebook(fragment: Fragment) {

        _userResource.send(Resource.Loading(true))

        callbackManager = CallbackManager.Factory.create()

        LoginManager
            .getInstance()
            .logInWithReadPermissions(
                fragment,
                listOf("email", "public_profile")
            )

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    fragment.lifecycleScope.launch {
                        val credential =
                            FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
                        val authResult = firebaseAuth.signInWithCredential(credential).await()

                        val firebaseUser = authResult.user

                        if (firebaseUser?.displayName != null) {
                            getUserFromFirestore(firebaseUser.uid).collect { result ->
                                when (result.status) {
                                    Status.LOADING -> {
                                        _userResource.send(Resource.Loading(true))
                                    }
                                    Status.SUCCESS -> {
                                        if (result.data != null) {
                                            _userResource.send(result)
                                        } else {
                                            val user = createUserObject(
                                                firebaseUser,
                                                firebaseUser.displayName as String
                                            )
                                            createUserInFirestore(user).collect { _result ->
                                                _userResource.send(_result)
                                            }

                                        }
                                    }
                                    Status.ERROR -> {
                                        result.message?.let { message ->
                                            _userResource.send(Resource.Error(message))
                                        }
                                    }
                                }
                            }
                        } else {
                            _userResource.send(Resource.Error(applicationContext.getString(R.string.error_during_registration_please_try_again_later)))
                        }
                    }
                }

                override fun onError(error: FacebookException?) {
                    fragment.lifecycleScope.launch {
                        error?.message?.let { message ->
                            _userResource.send(Resource.Error(message))
                        }
                    }
                }

                override fun onCancel() {
                }
            })
    }

    //Google
    override suspend fun signInWithGoogle(activity: Activity) {

        _userResource.send(Resource.Loading(true))

        val googleSignInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(applicationContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSingInClient = GoogleSignIn.getClient(activity, googleSignInOptions)

        val intent = googleSingInClient.signInIntent

        activity.startActivityForResult(intent, RC_SIGN_IN)
    }

    override suspend fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {

        val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
        if (account != null) {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user

            if (firebaseUser?.displayName != null) {
                getUserFromFirestore(firebaseUser.uid).collect { result ->
                    when (result.status) {
                        Status.LOADING -> {
                            _userResource.send(Resource.Loading(true))
                        }
                        Status.SUCCESS -> {
                            if (result.data != null) {
                                _userResource.send(result)
                            } else {
                                val user = createUserObject(
                                    firebaseUser,
                                    firebaseUser.displayName as String
                                )
                                createUserInFirestore(user).collect { _result ->
                                    _userResource.send(_result)
                                }

                            }
                        }
                        Status.ERROR -> {
                            result.message?.let { message ->
                                _userResource.send(Resource.Error(message))
                            }
                        }
                    }
                }
            } else {
                _userResource.send(Resource.Error(applicationContext.getString(R.string.error_during_registration_please_try_again_later)))
            }
        } else {
            _userResource.send(Resource.Error(applicationContext.getString(R.string.error_during_registration_please_try_again_later)))
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        coroutineScope: CoroutineScope
    ) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        println("$TAG -> $requestCode")
        Log.d(TAG, "$requestCode")
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            coroutineScope.launch {
                handleSignInResult(task)
            }
        }
    }


    override fun sendPasswordResetEmail(email: String) = flow<Resource<Nothing>> {
        emit(Resource.Loading(true))

        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(null))
        } catch (exception: Exception) {
            exception.message?.let {
                emit(Resource.Error(it))
            }
        }
    }.catch { exception ->

        exception.message?.let {
            emit(Resource.Error(it))
        }

    }.flowOn(IO)

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }


    override fun createUserInFirestore(user: User) = flow<Resource<User>> {

        usersCollectionReference.document(user.id).set(user).await()

        emit(Resource.Success(user))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun getUserFromFirestore(userId: String) = flow<Resource<User>> {

        val documentSnapshot = usersCollectionReference.document(userId).get().await()
        val user = documentSnapshot.toObject(User::class.java)

        emit(Resource.Success(user))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)


    override fun getFriendsAlphabeticalList(user: User) = flow<Resource<List<Friend>>> {

        emit(Resource.Loading(true))

        val result = if (lastResult == null) {
            friendsCollection
                .whereEqualTo("friendshipOwnerId", user.id)
                .orderBy("friendName", Query.Direction.ASCENDING)
                .limit(LIMIT_10)
                .get()
                .await()
        } else {
            friendsCollection
                .whereEqualTo("friendshipOwnerId", user.id)
                .orderBy("friendName", Query.Direction.ASCENDING)
                .startAfter(lastResult as DocumentSnapshot)
                .limit(LIMIT_10)
                .get()
                .await()
        }

        val documentsList = result.documents
        if (documentsList.size > 0) {
            lastResult = documentsList[documentsList.size - 1]
        }

        val listOfFriends = mutableListOf<Friend>()
        for (document in documentsList) {
            val friend = document.toObject(Friend::class.java)
            friend?.let {
                listOfFriends.add(friend)
            }
        }

        emit(Resource.Success(listOfFriends))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun searchFriends(searchText: String) = flow<Resource<List<User>>> {

        emit(Resource.Loading(true))

        val result = usersCollectionReference
            .whereGreaterThanOrEqualTo(FIELD_NAME, searchText)
            .whereLessThanOrEqualTo(FIELD_NAME, searchText + '\uf8ff')
            .orderBy(FIELD_NAME, Query.Direction.DESCENDING)
            .limit(LIMIT_20)
            .get()
            .await()

        val listOfUsers = mutableListOf<User>()

        for (document in result.documents) {
            document.toObject(User::class.java)?.let { user ->
                listOfUsers.add(user)
            }
        }

        emit(Resource.Success(listOfUsers))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun clearLastResultOfFriends() {
        lastResult = null
    }

    override fun getFriend(
        friendshipOwnerId: String,
        friendId: String
    ) = flow<Resource<Friend?>> {

        var friend: Friend? = null

        emit(Resource.Loading(true))

        val querySnapshot = friendsCollection
            .whereEqualTo("friendshipOwnerId", friendshipOwnerId)
            .whereEqualTo("friendId", friendId)
            .get().await()

        for (document in querySnapshot.documents) {
            friend = document.toObject(Friend::class.java)
            friend?.id = document.id
        }

        emit(Resource.Success(friend))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun getFriendRequest(requestOwnerId: String, requestPartnerId: String) = flow<Resource<FriendRequest?>> {

        var friendRequest: FriendRequest? = null

        emit(Resource.Loading(true))

        val querySnapshot = friendsRequestCollection
            .whereEqualTo("requestOwnerId", requestOwnerId)
            .whereEqualTo("requestPartnerId", requestPartnerId)
            .get().await()

        for (document in querySnapshot.documents) {
            friendRequest = document.toObject(FriendRequest::class.java)
            friendRequest?.id = document.id
        }

        emit(Resource.Success(friendRequest))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun getListOfFriendRequests(requestPartnerId: String) = flow<Resource<List<FriendRequest>>> {

        emit(Resource.Loading(true))

        val querySnapshot = friendsRequestCollection
            .whereEqualTo("requestPartnerId", requestPartnerId)
            .get()
            .await()

        val listOfRequests = mutableListOf<FriendRequest>()

        for(document in querySnapshot) {
            val friendRequest = document.toObject(FriendRequest::class.java)
            listOfRequests.add(friendRequest)
        }

        emit(Resource.Success(listOfRequests))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun sendFriendRequest(friendRequest: FriendRequest) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        friendsRequestCollection.document(friendRequest.id).set(friendRequest).await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun removeFriend(friend: Friend) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        friendsCollection.document(friend.id).delete().await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun createFriend(friend: Friend) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        friendsCollection.document(friend.id).set(friend).await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)

    override fun removeFriendRequest(friendRequest: FriendRequest) = flow<Resource<Nothing>> {

        emit(Resource.Loading(true))

        friendsRequestCollection.document(friendRequest.id).delete().await()

        emit(Resource.Success(null))

    }.catch { exception ->

        exception.message?.let { message ->
            emit(Resource.Error(message))
        }

    }.flowOn(Dispatchers.IO)






















}