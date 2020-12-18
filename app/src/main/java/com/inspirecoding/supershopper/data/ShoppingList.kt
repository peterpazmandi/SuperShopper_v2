package com.inspirecoding.supershopper.data

import java.util.*

data class ShoppingList(
    val id : String = "",
    val name  : String = "",
    val dueDate : Date = Date(),
    val friendsSharedWith: MutableList<String> = mutableListOf(),
    val itemsCount : Int = 0,
    val openItemsCount : Int = 0,
    val status : String = "",
    val comment : String = ""
) {
    val usersSharedWith: MutableList<User> = mutableListOf()

    fun calculateProgress(): Int {
        val rate = (openItemsCount/itemsCount)*100f
        return rate.toInt()
    }
    fun getTotalAndOpenItemsCount(): String {
        return "$itemsCount+$openItemsCount"
    }
}
