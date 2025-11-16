package com.example.messenger.util

sealed class NetworkUtils<T> {
    object Available: NetworkUtils<Nothing>()
    object UnAvailable: NetworkUtils<Nothing>()
    object Losing: NetworkUtils<Nothing>()
    object Lost: NetworkUtils<Nothing>()
    data class Error(val message: String): NetworkUtils<Nothing>()
}