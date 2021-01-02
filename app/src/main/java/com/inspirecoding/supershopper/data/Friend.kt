package com.inspirecoding.supershopper.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Friend(
    val friendshipOwnerId   : String = "",
    val friendId   : String = "",
    val friendName   : String = ""
): Parcelable
