package com.example.messenger.data.local.database

import androidx.room.TypeConverter
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.PhoneVisibiliity

class Converters {



    //PHONE CONVERTER
    @TypeConverter
    fun fromPhoneNumber(phone: PhoneNumber?): String?{
        return phone.let { "${it?.number},${it?.visibility}" }
    }
    @TypeConverter
    fun toPhoneNumber(phoneString: String?): PhoneNumber? {
        return phoneString?.split(",")?.let {
            PhoneNumber(number = it[0], visibility = PhoneVisibiliity.valueOf(it[1]))
        }
    }
    //PHONE CONVERTER
}