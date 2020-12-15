package com.inspirecoding.supershopper.data

data class Item(
    val id : String = "",
    val name  : String = "",
    val icon  : String = "",
    val status : String = "",
    val categoryId : Int = -1,
    val comment : String = ""
)