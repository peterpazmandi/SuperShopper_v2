package com.inspirecoding.supershopper.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class User(
    val id : String = "",
    val name : String = "",
    val profilePicture : String = "",
    val dateOfRegistration : Date = Date(),
    val numberOfFriends : Int = 0,
    val shoppingListCount : Int = 0,
    val hasSubscription : Boolean = false,
) : Parcelable
