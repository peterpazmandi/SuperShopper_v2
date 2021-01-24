package com.inspirecoding.supershopper.utils

import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.supershopper.data.User

object ObjectFactory {

    fun createUserObject(
        firebaseUser: FirebaseUser,
        username: String,
        profilePicture: String = ""
    ): User {

        val user = User(
            id =  firebaseUser.uid,
            name = username,
            profilePicture = profilePicture
        )

        firebaseUser.email?.let { _email ->
            user.emailAddress = _email
        }
        println("email -> $user")
        return user
    }
}