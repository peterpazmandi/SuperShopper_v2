package com.inspirecoding.supershopper.utils

import android.view.View
import android.widget.DatePicker
import com.google.android.material.snackbar.Snackbar
import java.util.*

fun View.showSnackbarWithOkButton(message: String) {
    val snackbar = Snackbar.make(this,
        message, Snackbar.LENGTH_INDEFINITE
    )
    snackbar.setAction(android.R.string.ok) {
        snackbar.dismiss()
    }
    snackbar.show()
}

fun View.makeItVisible() {
    this.visibility = View.VISIBLE
    this.alpha = 1f
}
fun View.makeItInVisible() {
    this.visibility = View.INVISIBLE
    this.alpha = 0f
}

fun DatePicker.convertSelectedDateToLong(): Long {

    val calender = Calendar.getInstance()
    calender.set(this.year, this.month, this.dayOfMonth)

    return calender.time.time

}