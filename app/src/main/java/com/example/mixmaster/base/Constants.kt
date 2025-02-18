package com.example.mixmaster.base

import com.example.mixmaster.model.Post

typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object COLLECTIONS {
        const val POSTS = "posts"
    }
}