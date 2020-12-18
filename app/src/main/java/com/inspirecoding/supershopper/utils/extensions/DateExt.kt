package com.inspirecoding.supershopper.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.getMonthLongName(): String {
    val calender = Calendar.getInstance()
    calender.time = this
    val df = SimpleDateFormat("MMMM", Locale.getDefault())
    val formattedDate = df.format(calender.time)
    return formattedDate.capitalize(Locale.getDefault())
}

fun Date.getDifferenceInDays(): Long {
    val calender = Calendar.getInstance()
    val millionSeconds = this.time - calender.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(millionSeconds)
}