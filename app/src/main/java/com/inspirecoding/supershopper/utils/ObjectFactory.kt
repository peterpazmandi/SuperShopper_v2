package com.inspirecoding.supershopper.utils

import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.supershopper.data.User

object ObjectFactory {

    fun createUserObject(firebaseUser: FirebaseUser, username: String, profilePicture: String = ""): User {
        val currentUser = User(
            id =  firebaseUser.uid,
            name = username,
            profilePicture = profilePicture
        )

        return currentUser
    }
}