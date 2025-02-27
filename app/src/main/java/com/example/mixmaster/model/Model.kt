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
import com.example.mixmaster.base.UsersCallback
import java.util.concurrent.Executors

class Model private constructor() {

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
                        if (post.author.isNotEmpty() && post.author != null) {
                            Log.d("TAG", "Fetching user for post: ${post.id}")
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

    fun searchPosts(query: String, callback: PostsCallback) {
        firebaseModel.searchPosts(query) { posts ->
            Log.d("TAG", "Search posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                // Count posts needing author enrichment.
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    // No enrichment needed—cache locally and return.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post { callback(posts) }
                } else {
                    // Create a mutable list for updating posts.
                    val updatedPosts = posts.toMutableList()
                    val counter = java.util.concurrent.atomic.AtomicInteger(postsToFetch)
                    for ((index, post) in updatedPosts.withIndex()) {
                        if (post.author.isNotEmpty()) {
                            getUser(post.author) { user ->
                                updatedPosts[index] = post.copy(
                                    authorName = user?.name ?: "",
                                    authorImage = user?.image ?: ""
                                )
                                if (counter.decrementAndGet() == 0) {
                                    // Once all user details are populated, cache locally.
                                    roomExecutor.execute {
                                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                                    }
                                    mainHandler.post { callback(updatedPosts) }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("TAG", "No search results from Firebase, using local DB")
                // Fallback to local database search.
                roomExecutor.execute {
                    // It is assumed that your local DAO provides a searchPosts function.
                    val localPosts = database.postDao().searchPosts(query)
                    if (localPosts.isNotEmpty()) {
                        val postsToFetch = localPosts.count { it.author.isNotEmpty() }
                        if (postsToFetch == 0) {
                            mainHandler.post { callback(localPosts) }
                        } else {
                            val updatedLocalPosts = localPosts.toMutableList()
                            val counter = java.util.concurrent.atomic.AtomicInteger(postsToFetch)
                            for ((index, post) in updatedLocalPosts.withIndex()) {
                                if (post.author.isNotEmpty()) {
                                    getUser(post.author) { user ->
                                        updatedLocalPosts[index] = post.copy(
                                            authorName = user?.name ?: "",
                                            authorImage = user?.image ?: ""
                                        )
                                        if (counter.decrementAndGet() == 0) {
                                            mainHandler.post { callback(updatedLocalPosts) }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        mainHandler.post { callback(localPosts) }
                    }
                }
            }
        }
    }

    fun getLastFourPosts(callback: PostsCallback) {
        firebaseModel.getLastFourPosts { posts ->
            Log.d("TAG", "Getting last four posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                // Count posts that have a non-empty author field.
                val postsToFetch = posts.count { it.author.isNotEmpty() }
                if (postsToFetch == 0) {
                    // No posts require author data – cache them locally and callback.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*posts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(posts)
                    }
                } else {
                    // Create a mutable copy to update posts with user info.
                    val updatedPosts = posts.toMutableList()
                    // Use an atomic counter to track pending user fetches.
                    val counter = java.util.concurrent.atomic.AtomicInteger(postsToFetch)
                    for ((index, post) in updatedPosts.withIndex()) {
                        if (post.author.isNotEmpty()) {
                            getUser(post.author) { user ->
                                updatedPosts[index] = post.copy(
                                    authorName = user?.name ?: "",
                                    authorImage = user?.image ?: ""
                                )
                                // When all user fetches are complete, update local DB and trigger the callback.
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
                Log.d("TAG", "Getting last four posts from local database")
                roomExecutor.execute {
                    // It is assumed that your local DB provides a method to retrieve the last four posts.
                    // If not, you might fetch all posts and then filter/sort to get the last four.
                    val localPosts = database.postDao().getLastFourPosts()
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun addPost(post: Post, profileImage: Bitmap?, callback: EmptyCallback) {
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
            } else {
                mainHandler.post { callback() }
            }
        }
    }

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        firebaseModel.getAllUserPosts(id) { posts ->
            Log.d("TAG", "Getting user posts from Firebase: $posts")
            if (posts.isNotEmpty()) {
                // Fetch the user details once, since all posts share the same author.
                getUser(id) { user ->
                    val updatedPosts = posts.map { post ->
                        post.copy(
                            authorName = user?.name ?: "",
                            authorImage = user?.image ?: ""
                        )
                    }
                    // Cache updated posts locally.
                    roomExecutor.execute {
                        database.postDao().insertPosts(*updatedPosts.toTypedArray())
                    }
                    mainHandler.post {
                        callback(updatedPosts)
                    }
                }
            } else {
                Log.d("TAG", "Getting user posts from local database")
                roomExecutor.execute {
                    val localPosts = database.postDao().getPostsByAuthor(id)
                    mainHandler.post {
                        callback(localPosts)
                    }
                }
            }
        }
    }

    fun getUser(id: String, callback: (User) -> Unit) {
        firebaseModel.getUser(id) { user ->
            if (user != null) {
                roomExecutor.execute {
                    database.userDao().insertUsers(user)
                }
                mainHandler.post { callback(user) }
            } else {
                roomExecutor.execute {
                    val localUser = database.userDao().getUserById(id)
                        ?: User(id, "Deleted User", "") // Fallback for deleted user.
                    mainHandler.post { callback(localUser) }
                }
            }
        }
    }

    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signIn(email, password, callback)
    }


    fun signUp(email: String, password: String, name: String, bio: String, bitmap: Bitmap?, callback: (FirebaseUser?, String?) -> Unit) {
        firebaseModel.signUp(email, password, name) { firebaseUser, error ->
            if (firebaseUser != null) {
                if (bitmap != null) {
                    // Upload the image to Cloudinary instead of Firebase Storage.
                    uploadImageToCloudinary(bitmap, firebaseUser.uid, onSuccess = { imageUrl ->
                        Log.d("TAG", "Image uploaded to Cloudinary: $imageUrl")
                        // Save user data to Firestore with the Cloudinary image URL.
                        firebaseModel.saveUser(firebaseUser, name, bio, imageUrl) { success, saveError ->
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
                        firebaseModel.saveUser(firebaseUser, name, bio, "") { success, saveError ->
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
                    firebaseModel.saveUser(firebaseUser, name, bio, "") { success, saveError ->
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

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers { users ->
            if (users.isNotEmpty()) {
                roomExecutor.execute {
                    database.userDao().insertUsers(*users.toTypedArray())
                }
                mainHandler.post {
                    callback(users)
                }
            } else {
                roomExecutor.execute {
                    val localUsers = database.userDao().getAllUsers()
                    mainHandler.post {
                        callback(localUsers)
                    }
                }
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