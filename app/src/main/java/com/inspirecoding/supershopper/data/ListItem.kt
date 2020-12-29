package com.inspirecoding.supershopper.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListItem(
    val id: String = "",
    var item: String = "",
    var unit: String = "",
    var qunatity: Float = 0f,
    var isBought: Boolean = false,
    var priority: String = "",
    val icon: String = "",
    val categoryId: Int? = null,
    val comment: String = ""
) : Parcelable