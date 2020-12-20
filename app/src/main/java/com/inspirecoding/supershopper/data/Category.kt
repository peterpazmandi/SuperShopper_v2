package com.inspirecoding.supershopper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(

    @PrimaryKey(autoGenerate = true)
    val id: Int = -1,

    val customName: String? = null,
    val icon: String = "",
    val nameStringResId: Int? = null,
    val position : Int = -1
)
