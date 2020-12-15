package com.inspirecoding.supershopper.data

import java.util.*

data class ShoppingList(
    val id : String = "",
    val name  : String = "",
    val dueDate : Date = Date(),
    val itemsCount : Int = 0,
    val openItemsCount : Int = 0,
    val status : String = "",
    val comment : String = ""
)
