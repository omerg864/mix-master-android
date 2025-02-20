package com.example.mixmaster.restAPI

import com.example.mixmaster.model.AiRequest
import com.example.mixmaster.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.Headers

interface CocktailApiService {

    // POST /api/ai returns a single Post.
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/ai")
    suspend fun getAiCocktail(
        @Body request: AiRequest,
    ): Response<Post>

    // GET /api/db/random returns a list of posts.
    @Headers("Accept: application/json")
    @GET("api/db/random")
    suspend fun getRandomCocktails(
    ): Response<RandomCocktailResponse>

    // GET /api/db/{id} returns a single Post.
    @Headers("Accept: application/json")
    @GET("api/db/{id}")
    suspend fun getCocktailById(
        @Path("id") id: String
    ): Response<Post>
}