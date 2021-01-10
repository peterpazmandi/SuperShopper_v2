package com.inspirecoding.supershopper.repository.user

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.UploadTask
import com.inspirecoding.supershopper.data.Friend
import com.inspirecoding.supershopper.data.FriendRequest
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface UserRepository {

    val userResource: Flow<Resource<User>>

    suspend fun registerUserFromAuthWithEmailAndPassword(
        username: String,
        email: String,
        password: String
    )

    suspend fun logInUserFromAuthWithEmailAndPassword(email: String, password: String)

    suspend fun signInWithFacebook(fragment: Fragment)
    suspend fun signInWithGoogle(activity: Activity)
    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        coroutineScope: CoroutineScope
    )

    suspend fun handleSignInResult(completedTask: Task<GoogleSignInAccount>)
    suspend fun checkUserLoggedIn(coroutineScope: CoroutineScope)

    fun createUserInFirestore(user: User): Flow<Resource<User>>
    fun getUserFromFirestore(userId: String): Flow<Resource<User>>
    fun updateCurrentUserEmail(email: String): Flow<Resource<Nothing>>
    fun updateCurrentUserPassword(password: String): Flow<Resource<Nothing>>

    fun sendPasswordResetEmail(email: String): Flow<Resource<Nothing>>
    suspend fun logOut()

    suspend fun updateProfilePictureOfUserInFirestore(user: User): Flow<Resource<Void?>>
    suspend fun updateNameOFUserInFirestore(user: User): Flow<Resource<Void?>>
    fun updateFirebaseInstanceTokenOFUserInFirestore(user: User, coroutineScope: CoroutineScope): Flow<Resource<Void?>>
    fun getFirebaseInstanceToken(): Flow<String>
    suspend fun uploadProfilePictureOfUserToStorage(user: User): Flow<Resource<User>>

    fun getFriendsAlphabeticalList(user: User): Flow<Resource<List<Friend>>>
    fun getAllFriends(user: User): Flow<Resource<List<Friend>>>
    fun searchFriends(searchText: String): Flow<Resource<List<User>>>
    fun updateFriendName(friendId: String, newName: String): Flow<Resource<Nothing>>
    fun clearLastResultOfFriends()

    fun createFriend(friend: Friend): Flow<Resource<Nothing>>
    fun removeFriend(friend: Friend): Flow<Resource<Nothing>>
    fun getFriend(friendshipOwnerId: String, friendId: String): Flow<Resource<Friend?>>
    fun getFriendRequest(
        requestOwnerId: String,
        requestPartnerId: String
    ): Flow<Resource<FriendRequest?>>
    fun getListOfFriendRequests(requestPartnerId: String): Flow<Resource<List<FriendRequest>>>

    fun sendFriendRequest(friendRequest: FriendRequest): Flow<Resource<Nothing>>
    fun removeFriendRequest(friendRequest: FriendRequest): Flow<Resource<Nothing>>
}