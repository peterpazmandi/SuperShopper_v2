package com.inspirecoding.supershopper.data

data class ListItem(
    val id: String = "",
    var item: String = "",
    var unit: String = "",
    var qunatity: Float = 0f,
    var isBought: Boolean = false,
    var priority: String = "",
    val icon: String = "",
    val categoryId: Int = -1,
    val comment: String = ""
)