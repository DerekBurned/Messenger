package com.example.messenger.data.local.database

import androidx.room.TypeConverter
import com.example.messenger.domain.model.MessageStatus
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.PhoneVisibiliity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // PHONE CONVERTER
    @TypeConverter
    fun fromPhoneNumber(phone: PhoneNumber?): String? {
        return phone?.let { "${it.number},${it.visibility}" }
    }

    @TypeConverter
    fun toPhoneNumber(phoneString: String?): PhoneNumber? {
        return phoneString?.split(",")?.let {
            PhoneNumber(number = it[0], visibility = PhoneVisibiliity.valueOf(it[1]))
        }
    }

    // LIST<STRING> CONVERTERS (for participantIds, participantNames, etc.)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromNullableStringList(value: List<String?>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toNullableStringList(value: String?): List<String?>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String?>>() {}.type
        return gson.fromJson(value, listType)
    }

    // MESSAGE STATUS CONVERTER
    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus {
        return MessageStatus.valueOf(value)
    }
}