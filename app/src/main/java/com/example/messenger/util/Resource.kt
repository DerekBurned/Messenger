package com.example.messenger.util

sealed class Resource<out T> {
    data class Success<T>(val data:T): Resource<T>()
    data class Error(val message:String): Resource<Nothing>()
    data class Failure(val exception: Exception): Resource<Nothing>()
    object Loading: Resource<Nothing>()
}