package com.example.mixmaster.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mixmaster.model.Post

@Dao
interface PostDao {

    @Query("SELECT * FROM Post")
    fun getAllPosts(): List<Post>

    @Query("SELECT * FROM Post WHERE id =:id")
    fun getPostById(id: String): Post

    @Query("SELECT * FROM Post WHERE author =:author")
    fun getPostsByAuthor(author: String): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(vararg posts: Post)

    @Delete
    fun deletePost(post: Post)
}