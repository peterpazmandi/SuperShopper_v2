package com.inspirecoding.supershopper.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListItem(
    var id: String = "",
    var item: String = "",
    var unit: String = "",
    var qunatity: Float = 0f,
    var isBought: Boolean = false,
    var priority: String = "",
    var icon: String = "",
    var categoryId: Int? = null,
    var comment: String = ""
) : Parcelable