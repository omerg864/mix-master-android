package com.example.mixmaster.model

data class Post(
    val authorName: String,
    val authorImage: String,
    val postTime: String,
    val likes: Int,
    val comments: Int,
    val id: Int,
    val name: String,
    val description: String,
    val images: List<String>,
)