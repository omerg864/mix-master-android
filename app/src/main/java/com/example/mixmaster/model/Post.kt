package com.example.mixmaster.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String,
    val name: String,
    val image: String,
    val description: String = "",
    val author: String = "",
) {

    companion object {

        private const val ID_KEY = "id"
        private const val NAME_KEY = "name"
        private const val IMAGE_KEY = "image"
        private const val DESCRIPTION_KEY = "description"
        private const val AUTHOR_KEY = "author"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: ""
            val name = json[NAME_KEY] as? String ?: ""
            val image = json[IMAGE_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""

            return Post(
                id = id,
                name = name,
                image = image,
                description = description,
                author = author
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            NAME_KEY to name,
            IMAGE_KEY to image,
            DESCRIPTION_KEY to description,
            AUTHOR_KEY to author
        )
}