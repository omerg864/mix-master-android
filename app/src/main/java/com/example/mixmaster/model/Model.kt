package com.example.mixmaster.model

import android.graphics.Bitmap
import android.util.Log
import com.example.mixmaster.base.EmptyCallback
import com.example.mixmaster.base.PostsCallback

class Model private constructor() {

    enum class Storage {
        FIREBASE,
        CLOUDINARY
    }

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts(callback)
    }

    fun addPost(post: Post, profileImage: Bitmap?, storage: Storage, callback: EmptyCallback) {
        firebaseModel.addPost(post) {
            profileImage?.let {

                when (storage) {
                    Storage.FIREBASE -> {
                        uploadImageToFirebase(
                            image = it,
                            name = post.id) { url ->
                            url?.let {
                                val pt = post.copy(image = it)
                                firebaseModel.addPost(pt, callback)
                            } ?: callback()
                        }
                    }
                    Storage.CLOUDINARY -> {
                        uploadImageToCloudinary(
                            image = it,
                            name = post.id,
                            onSuccess = { url ->
                                val pt = post.copy(image = url)
                                firebaseModel.addPost(pt, callback)
                            },
                            onError = { callback() }
                        )
                    }
                }
            } ?: callback()
        }
    }

    private fun uploadImageToFirebase(image: Bitmap, name: String, callback: (String?) -> Unit) {
        firebaseModel.uploadImage(image, name, callback)
    }

    private fun uploadImageToCloudinary(image: Bitmap, name: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        cloudinaryModel.uploadBitmap(
            bitmap = image,
//            name = name,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}