package com.example.mixmaster.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val image: String = "",
    val bio: String = "",
) {

    companion object {

        private const val ID_KEY = "id"
        private const val NAME_KEY = "name"
        private const val IMAGE_KEY = "image"
        private const val BIO_KEY = "bio"

        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: ""
            val name = json[NAME_KEY] as? String ?: ""
            val image = json[IMAGE_KEY] as? String ?: ""
            val bio = json[BIO_KEY] as? String ?: ""

            return User(
                id = id,
                name = name,
                image = image,
                bio = bio,
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            NAME_KEY to name,
            IMAGE_KEY to image,
            BIO_KEY to bio,
        )
}