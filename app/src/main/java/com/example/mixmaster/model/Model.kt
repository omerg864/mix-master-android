package com.example.mixmaster.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import com.example.mixmaster.base.EmptyCallback
import com.example.mixmaster.base.PostsCallback
import com.example.mixmaster.model.dao.AppLocalDb
import com.example.mixmaster.model.dao.AppLocalDbRepository
import com.google.firebase.auth.FirebaseUser
import android.os.Handler  // Use Android's Handler, not java.util.logging.Handler
import java.util.concurrent.Executors

class Model private constructor() {

    enum class Storage {
        FIREBASE,
        CLOUDINARY
    }

    private val firebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()
    private val database: AppLocalDbRepository = AppLocalDb.database
    private var roomExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = Model()
    }

    fun getAllPosts(callback: PostsCallback) {
        firebaseModel.getAllPosts { posts ->
            Log.d("TAG", "Getting posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                // Count how many posts have a non-empty author field.
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    // No posts need author info – store and return immediately.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(posts)
                    }
                } else {
                    // Create a mutable copy to update each post with user info.
                    val updatedPosts = posts.toMutableList()
                    // Use an atomic counter to track the number of pending user fetches.
                    val counter = java.util.concurrent.atomic.AtomicInteger(postsToFetch)
                    for ((index, post) in updatedPosts.withIndex()) {
                        if (post.author.isNotEmpty()) {
                            // For each post with a valid author, fetch the user.
                            getUser(post.author) { user ->
                                // When user data is fetched, update the post.
                                updatedPosts[index] = post.copy(
                                    authorName = user?.name ?: "",
                                    authorImage = user?.image ?: ""
                                )
                                // When all user fetches are done, update the local DB and callback.
                                if (counter.decrementAndGet() == 0) {
                                    roomExecutor.execute {
                                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                                    }
                                    mainHandler.post {
                                        callback(updatedPosts)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("TAG", "Getting posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getAllPosts()
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun addPost(post: Post, profileImage: Bitmap?, storage: Storage, callback: EmptyCallback) {
        // Attempt to add the post to Firebase first.
        firebaseModel.addPost(post) { firebaseSuccess ->
            if (!firebaseSuccess) {
                Log.d("TAG", "Firebase add failed")
                mainHandler.post { callback() }
                return@addPost
            }

            Log.d("TAG", "Firebase add succeeded")
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
                                firebaseModel.addPost(updatedPost) { updateSuccess ->
                                    if (updateSuccess) {
                                        roomExecutor.execute {
                                            database.postDao().insertPosts(updatedPost)
                                        }
                                    }
                                    mainHandler.post { callback() }
                                }
                            } else {
                                mainHandler.post { callback() }
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
                                    mainHandler.post { callback() }
                                }
                            },
                            onError = {
                                mainHandler.post { callback() }
                            }
                        )
                    }
                }
            } else {
                mainHandler.post { callback() }
            }
        }
    }

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        firebaseModel.getAllUserPosts(id) { posts ->
            if (posts.isNotEmpty()) {
                roomExecutor.execute {
                    database.postDao().insertPosts(*posts.toTypedArray())
                }
                mainHandler.post { callback(posts) }
            } else {
                Log.d("TAG", "Getting user posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getPostsByAuthor(id)
                    mainHandler.post { callback(localPosts) }
                }
            }
        }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        firebaseModel.getUser(id) { user ->
            // Post the result to the main thread.
            if (user != null) {
                roomExecutor.execute {
                    database.userDao().insertUsers(user)
                }
                mainHandler.post { callback(user) }
            } else {
                roomExecutor.execute {
                    val localUser = database.userDao().getUserById(id)
                    mainHandler.post { callback(localUser) }
                }
            }
        }
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }


    fun signUp(email: String, password: String, name: String, bitmap: Bitmap?, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signUp(email, password, name) { firebaseUser, error ->
            if (firebaseUser != null) {
                if (bitmap != null) {
                    // Upload the image to Cloudinary instead of Firebase Storage.
                    uploadImageToCloudinary(bitmap, firebaseUser.uid, onSuccess = { imageUrl ->
                        Log.d("TAG", "Image uploaded to Cloudinary: $imageUrl")
                        // Save user data to Firestore with the Cloudinary image URL.
                        firebaseModel.saveUser(firebaseUser, name, imageUrl) { success, saveError ->
                            if (success) {
                                // Save the user locally.
                                roomExecutor.execute {
                                    database.userDao().insertUsers(User(firebaseUser.uid, name, imageUrl))
                                }
                                mainHandler.post { callback(firebaseUser, null) }
                            } else {
                                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                            }
                        }
                    }, onError = { errMsg ->
                        Log.e("TAG", "Image upload to Cloudinary failed: $errMsg")
                        // If Cloudinary upload fails, save the user without an image.
                        firebaseModel.saveUser(firebaseUser, name, "") { success, saveError ->
                            if (success) {
                                roomExecutor.execute {
                                    database.userDao().insertUsers(User(firebaseUser.uid, name, ""))
                                }
                                mainHandler.post { callback(firebaseUser, null) }
                            } else {
                                mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                            }
                        }
                    })
                } else {
                    // No image provided; save user with an empty image field.
                    firebaseModel.saveUser(firebaseUser, name, "") { success, saveError ->
                        if (success) {
                            roomExecutor.execute {
                                database.userDao().insertUsers(User(firebaseUser.uid, name, ""))
                            }
                            mainHandler.post { callback(firebaseUser, null) }
                        } else {
                            mainHandler.post { callback(null, saveError ?: "Error saving user to Firestore") }
                        }
                    }
                }
            } else {
                mainHandler.post { callback(null, error ?: "Sign up failed") }
            }
        }
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
            onSuccess = onSuccess,
            onError = onError
        )
    }
}