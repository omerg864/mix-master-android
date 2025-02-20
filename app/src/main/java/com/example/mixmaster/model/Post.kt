package com.example.mixmaster.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val image: String = "",
    val description: String = "",
    val instructions: String = "",
    val ingredients: String = "",
    val author: String = "",      // The user ID of the author
    val authorName: String = "",  // Will be updated after fetching the user details
    val authorImage: String = ""  // Will be updated after fetching the user details
) {
    companion object {
        private const val ID_KEY = "id"
        private const val NAME_KEY = "name"
        private const val IMAGE_KEY = "image"
        private const val AUTHOR_KEY = "author"
        private const val DESCRIPTION_KEY = "description"
        private const val INSTRUCTIONS_KEY = "instructions"
        private const val INGREDIENTS_KEY = "ingredients"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? String ?: ""
            val name = json[NAME_KEY] as? String ?: ""
            val image = json[IMAGE_KEY] as? String ?: ""
            val author = json[AUTHOR_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val instructions = json[INSTRUCTIONS_KEY] as? String ?: ""
            val ingredients = json[INGREDIENTS_KEY] as? String ?: ""
            // When deserializing from JSON, authorName and authorImage will be empty;
            // they will be updated later once you fetch the user details.
            return Post(id = id, name = name, image = image, author = author, description = description, instructions = instructions, ingredients = ingredients)
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            NAME_KEY to name,
            IMAGE_KEY to image,
            AUTHOR_KEY to author,
            DESCRIPTION_KEY to description,
            INSTRUCTIONS_KEY to instructions,
            INGREDIENTS_KEY to ingredients
        )
}