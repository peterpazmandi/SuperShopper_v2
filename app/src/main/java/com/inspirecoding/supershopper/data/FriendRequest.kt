package com.inspirecoding.supershopper.data

import java.util.*

data class FriendRequest(
    var id : String = "",
    var friendshipStatus : String = "",
    var requestOwnerId : String = "",
    var requestPartnerId : String = "",
    var requestDate : Date = Date()
)
