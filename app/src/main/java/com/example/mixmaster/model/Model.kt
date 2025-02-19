package com.example.mixmaster.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import com.example.mixmaster.base.EmptyCallback
import com.example.mixmaster.base.PostsCallback
import com.example.mixmaster.model.dao.AppLocalDb
import com.example.mixmaster.model.dao.AppLocalDbRepository
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executors
import java.util.logging.Handler

class Model private constructor() {

    enum class Storage {
        FIREBASE,
        CLOUDINARY
    }

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()
    private val database: AppLocalDbRepository = AppLocalDb.database;
    private var roomExecutor = Executors.newSingleThreadExecutor()

    companion object {
        val shared = Model()
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts {
            Log.d("TAG", "Getting posts from Firebase")
            Log.d("TAG", it.toString())
            if (it.isNotEmpty()) {
                roomExecutor.execute {
                    database.postDao().insertPosts(*it.toTypedArray())
                }
                callback(it)
            } else {
                Log.d("TAG", "Getting posts from local database")
                roomExecutor.execute {
                    val posts = database.postDao().getAllPosts()
                    android.os.Handler(Looper.getMainLooper()).post {
                        callback(posts)
                    }
                }
            }
        }
    }

    fun addPost(post: Post, profileImage: Bitmap?, storage: Storage, callback: EmptyCallback) {
        // Attempt to add the post to Firebase first.
        firebaseModel.addPost(post) { firebaseSuccess ->
            if (!firebaseSuccess) {
                // Firebase addition failed; do not add to the local database.
                Log.d("TAG", "Firebase add failed")
                callback()
                return@addPost;
            }

            Log.d("TAG", "Firebase add succeeded")

            // Firebase add succeeded—insert the post into the local database.
            roomExecutor.execute {
                database.postDao().insertPosts(post)
            }

            // If a profile image is provided, upload it and update the post.
            if (profileImage != null) {
                when (storage) {
                    Storage.FIREBASE -> {
                        uploadImageToFirebase(image = profileImage, name = post.id) { url ->
                            if (url != null) {
                                val updatedPost = post.copy(image = url)
                                // Update the post in Firebase with the new image URL.
                                firebaseModel.addPost(updatedPost) { updateSuccess ->
                                    if (updateSuccess) {
                                        // Update local database with the updated post.
                                        roomExecutor.execute {
                                            database.postDao().insertPosts(updatedPost)
                                        }
                                    }
                                    callback()
                                }
                            } else {
                                // Image upload failed; finish callback.
                                callback()
                            }
                        }
                    }
                    Storage.CLOUDINARY -> {
                        uploadImageToCloudinary(
                            image = profileImage,
                            name = post.id,
                            onSuccess = { url ->
                                val updatedPost = post.copy(image = url)
                                firebaseModel.addPost(updatedPost) { updateSuccess ->
                                    if (updateSuccess) {
                                        roomExecutor.execute {
                                            database.postDao().insertPosts(updatedPost)
                                        }
                                    }
                                    callback()
                                }
                            },
                            onError = {
                                callback()
                            }
                        )
                    }
                }
            } else {
                // No image to upload; finish.
                callback()
            }
        }
    }


    fun getAllUserPosts(id: String, callback: PostsCallback) {
        firebaseModel.getAllUserPosts(id) {
            if (it.isNotEmpty()) {
                roomExecutor.execute {
                    database.postDao().insertPosts(*it.toTypedArray())
                }
                callback(it)
            } else {
                Log.d("TAG", "Getting user posts from local database")
                roomExecutor.execute {
                    val posts = database.postDao().getPostsByAuthor(id)
                    android.os.Handler(Looper.getMainLooper()).post {
                        callback(posts)
                    }
                }
            }
        }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        firebaseModel.getUser(id, callback)
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }

    fun signUp(email: String, password: String, name: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signUp(email, password, name, callback)
    }

    fun signOut() {
        firebaseModel.signOut()
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