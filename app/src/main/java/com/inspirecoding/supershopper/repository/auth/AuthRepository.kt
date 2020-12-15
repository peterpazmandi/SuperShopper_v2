package com.inspirecoding.supershopper.repository.auth

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.supershopper.data.Resource
import com.inspirecoding.supershopper.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val userResource: Flow<Resource<User>>

    suspend fun registerUserFromAuthWithEmailAndPassword(
        username: String,
        email: String,
        password: String
    )

    suspend fun logInUserFromAuthWithEmailAndPassword(
        email: String,
        password: String
    )

    suspend fun signInWithFacebook(fragment: Fragment)
    suspend fun signInWithGoogle(activity: Activity)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, coroutineScope: CoroutineScope)
    suspend fun handleSignInResult(completedTask: Task<GoogleSignInAccount>)
    suspend fun checkUserLoggedIn(coroutineScope: CoroutineScope)

    suspend fun getUserFromFirestore(userId: String): Flow<Resource<User>>

    suspend fun sendPasswordResetEmail(email: String): Flow<Resource<Nothing>>
    suspend fun signOut()

//    suspend fun getUserFromFirestore(userId: String): Resource<User>?
//
//    suspend fun registerUserFromAuthWithEmailAndPassword(
//        email: String,
//        password: String,
//        context: Context
//    ): Resource<FirebaseUser?>

//    suspend fun sendPasswordResetEmail(
//        email: String
//    ): Resource<Void?>
//
//    suspend fun checkUserLoggedIn(): FirebaseUser?
//    suspend fun logOutUser()
//
//    suspend fun signInWithCredential(
//        authCredential: AuthCredential
//    ): Resource<AuthResult?>

}