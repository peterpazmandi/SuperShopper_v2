package com.inspirecoding.supershopper.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

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
    this.alpha = 1f
}
fun View.makeItInVisible() {
    this.alpha = 0f
}