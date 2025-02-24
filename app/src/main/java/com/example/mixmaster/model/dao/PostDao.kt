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

    @Query("SELECT * FROM Post LIMIT 4")
    fun getLastFourPosts(): List<Post>

    @Query("SELECT * FROM Post WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR instructions LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%'")
    fun searchPosts(query: String): List<Post>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(vararg posts: Post)

    @Delete
    fun deletePost(post: Post)
}