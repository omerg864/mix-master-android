package com.example.mixmaster.model

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.storage.storage
import com.example.mixmaster.base.Constants
import com.example.mixmaster.base.EmptyCallback
import com.example.mixmaster.base.PostsCallback
import com.example.mixmaster.base.SuccessCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val database = Firebase.firestore
    private val storage = Firebase.storage
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }

        database.firestoreSettings = setting
    }

    fun getAllPosts(callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun getAllUserPosts(id: String, callback: PostsCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).whereEqualTo("author", id).get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                        }
                        callback(posts)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun addPost(post: Post, callback: SuccessCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id)
            .set(post.json)
            .addOnCompleteListener {
                callback(it.isSuccessful)
            }
    }

    fun deletePost(id: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(id).delete()
            .addOnCompleteListener {
                callback()
            }
    }

    fun signUp(email: String, password: String, name: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    callback(user, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }


    fun signIn(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(auth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    // --------------------
    // User Data Functions
    // --------------------
    fun saveUser(user: FirebaseUser, name: String, image: String?, callback: (Boolean, String?) -> Unit) {
        // Define the user data you want to store.
        val userData = hashMapOf(
            "id" to user.uid,
            "image" to image,
            "name" to name,
        )

        // Save the data in the "users" collection with the document id as the user's uid.
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.uid)
            .set(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun getUser(id: String, callback: (User?) -> Unit) {
        database.collection(Constants.COLLECTIONS.USERS).document(id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
    }

    fun uploadImage(image: Bitmap, name: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageProfileRef = storageRef.child("images/$name.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageProfileRef.putBytes(data)
        uploadTask
            .addOnFailureListener { callback(null) }
            .addOnSuccessListener { taskSnapshot ->
                imageProfileRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
    }
}