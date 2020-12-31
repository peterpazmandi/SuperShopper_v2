package com.inspirecoding.supershopper.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*


@Parcelize
data class ShoppingList(
    var shoppingListId: String = "",
    var name: String = "",
    var dueDate: Date = Date(),
    var timeStamp: Long = 0,
    var shoppingListStatus: String = "",
    val friendsSharedWith: MutableList<String> = mutableListOf<String>(),
    val usersSharedWith: MutableList<User> = mutableListOf(),
    var listOfItems: MutableList<ListItem> = mutableListOf<ListItem>(),
    val status: String = "",
    val comment: String = ""
): Parcelable {

    fun calculateProgress(): Int {
        val itemsCount = listOfItems.size
        val openItemsCount = listOfItems.filter {
            it.isBought
        }.size
        val rate = (openItemsCount.toFloat() / itemsCount.toFloat()) * 100f
        return rate.toInt()
    }

    fun getTotalAndOpenItemsCount(): String {
        val itemsCount = listOfItems.size
        val openItemsCount = listOfItems.filter {
            it.isBought
        }.size
        return "$itemsCount/$openItemsCount"
    }
}
