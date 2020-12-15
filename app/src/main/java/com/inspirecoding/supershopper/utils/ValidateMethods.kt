package com.inspirecoding.supershopper.utils

import android.util.Patterns

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

}