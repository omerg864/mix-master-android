package com.example.mixmaster.base

import com.example.mixmaster.model.Post
import com.example.mixmaster.model.User

typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit
typealias SuccessCallback = (Boolean) -> Unit
typealias UsersCallback = (List<User>) -> Unit

object Constants {

    object COLLECTIONS {
        const val POSTS = "posts"
        const val USERS = "users"
    }
}