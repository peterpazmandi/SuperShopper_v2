package com.inspirecoding.supershopper.utils

import android.util.Patterns
import com.inspirecoding.supershopper.data.Category
import java.util.*

object ValidateMethods {

    fun validateEmail(email: String): String {
        val emailInput: String = email.trim()
        return when {
            emailInput.isEmpty() -> {
                "Email field can't be empty"
            }
            !Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() -> {
                "Please enter a valid email address"
            }
            else -> {
                ""
            }
        }
    }

    fun validateUsername(username: String): String {
        val usernameInput: String = username.trim()
        return when {
            usernameInput.isEmpty() -> {
                "Username field can't be empty"
            }
            usernameInput.length < 5 -> {
                "Username must be at least 5 characters long"
            }
            usernameInput.length > 15 -> {
                "Username can be up to 15 characters long"
            }
            else -> {
                ""
            }
        }
    }

    fun validatePassword(username: String): String {
        val usernameInput: String = username.trim()
        return when {
            usernameInput.isEmpty() -> {
                "Password field can't be empty"
            }
            usernameInput.length < 5 -> {
                "Password must be at least 5 characters long"
            }
            else -> {
                ""
            }
        }
    }

    fun validateName(name: String?): String {
        return when {
            name.isNullOrEmpty() -> {
                "Name can't be empty"
            }
            name.length < 3 -> {
                "Name must be at least 3 characters long"
            }
            else -> {
                ""
            }
        }
    }

    fun validateUnit(unit: String?): String {
        val usernameInput: String? = unit?.trim()
        return when {
            usernameInput.isNullOrEmpty() -> {
                "You have not selected a unit"
            }
            else -> {
                ""
            }
        }
    }

    fun validateQuantity(quantity: Float): String {
        return when (quantity) {
            0f -> {
                "You did not enter a quantity"
            }
            else -> {
                ""
            }
        }
    }

    fun validateCategory(category: Category?): String {
        return when (category) {
            null -> {
                "You did not select a category"
            }
            else -> {
                ""
            }
        }
    }

}