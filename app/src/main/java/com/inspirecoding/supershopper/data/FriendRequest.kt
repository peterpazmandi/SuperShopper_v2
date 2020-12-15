package com.inspirecoding.supershopper.data

import java.util.*

data class FriendRequest(
    val id : String = "",
    val sender : String = "",
    val receiver : String = "",
    val requestDate : Date = Date()
)
