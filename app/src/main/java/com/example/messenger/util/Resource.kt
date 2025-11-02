package com.example.messenger.util

import androidx.compose.ui.Modifier

sealed class Resource<out T> {
    data class Success<T>(val data:T): Resource<T>()
    data class Error(val messsage: String): Resource<Nothing>()
    object Loading: Resource<Nothing>()
}