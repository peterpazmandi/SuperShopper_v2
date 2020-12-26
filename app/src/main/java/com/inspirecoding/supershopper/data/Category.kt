package com.inspirecoding.supershopper.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Category(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val customName: String? = null,
    val iconDrawableResId: Int = -1,
    val nameStringResId: Int? = null,
    var position : Int = -1
) : Parcelable
