package com.example.mixmaster.restAPI

import com.example.mixmaster.model.Post

data class RandomCocktailResponse(
    val success: Boolean,
    val cocktails: List<Post>
)