package com.example.mixmaster.model

data class Post(
    val id: Int,
    val name: String,
    val description: String,
    val images: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>,
)