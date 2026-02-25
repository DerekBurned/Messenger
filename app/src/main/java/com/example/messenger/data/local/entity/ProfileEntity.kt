package com.example.messenger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.messenger.domain.model.PhoneNumber
import com.example.messenger.domain.model.PhoneVisibiliity

@Entity(tableName = "profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["user_id"])
    ]
)
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val username: String?,
    val email: String? = null,
    @ColumnInfo(name = "user_id")
    val userId : String,
    val avatarUrl: String? = null,
    val lastSeen: Long = 0L,
    val isOnline: Boolean = false,
    val phoneVisibility: PhoneVisibiliity = PhoneVisibiliity.HIDDEN,
    val fcmToken: String? = null
) {
}