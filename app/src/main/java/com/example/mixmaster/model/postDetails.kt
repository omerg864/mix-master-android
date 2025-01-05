package com.example.mixmaster.model

data class PostDetails(
    val authorName: String,
    val authorImage: String,
    val postTime: String,
    val likes: List<String>,
    val comments: List<String>,
    val id: Int,
    val name: String,
    val description: String,
    val images: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>,
)