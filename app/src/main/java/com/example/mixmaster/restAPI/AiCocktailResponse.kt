package com.example.mixmaster.restAPI

import com.example.mixmaster.model.Post

data class AiCocktailResponse(
    val success: Boolean,
    val cocktail: Post
)