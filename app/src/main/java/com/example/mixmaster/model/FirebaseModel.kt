package com.example.mixmaster.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.storage.storage
import com.example.mixmaster.base.Constants
import com.example.mixmaster.base.EmptyCallback
import com.example.mixmaster.base.PostsCallback
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val database = Firebase.firestore
    private val storage = Firebase.storage
    private var auth = Firebase.auth

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
                        val students: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            students.add(Post.fromJSON(json.data))
                        }
                        callback(students)
                    }
                    false -> callback(listOf())
                }
            }
    }

    fun addPost(post: Post, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(post.id)
            .set(post.json)
            .addOnCompleteListener {
                callback()
            }
    }

    fun deletePost(id: String, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.POSTS).document(id).delete().addOnCompleteListener{
            callback()
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