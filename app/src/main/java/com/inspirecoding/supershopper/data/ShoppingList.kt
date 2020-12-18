package com.inspirecoding.supershopper.data

import java.util.*

data class ShoppingList(
    var shoppingListId: String = "",
    val name: String = "",
    val dueDate: Date = Date(),
    var timeStamp: Long = 0,
    var shoppingListStatus: String = "",
    val friendsSharedWith: MutableList<String> = mutableListOf<String>(),
    var listOfItems: MutableList<ListItem> = mutableListOf<ListItem>(),
    val status: String = "",
    val comment: String = ""
) {
    val usersSharedWith: MutableList<User> = mutableListOf()

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
