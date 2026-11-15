package com.example.actividad3da

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class Municipio(val name: String, val population: Int, val temperature: Double)
data class User(val username: String, val password: String, val email: String)

interface TitanApi {
    // ---- Municipios (/xapi/table) ----
    @GET("xapi/table") suspend fun getTables(): List<Municipio>
    @PUT("xapi/table") suspend fun addTable(@Body m: Municipio): Response<Unit>
    @POST("xapi/table") suspend fun updateTable(@Body m: Municipio): Response<Unit>
    @DELETE("xapi/table") suspend fun deleteTable(@Query("name") name: String): Response<Unit>

    // ---- Usuarios (/xapi/user) ----
    @GET("xapi/user") suspend fun getUsers(): List<User>
    @PUT("xapi/user") suspend fun addUser(@Body u: User): Response<Unit>
    @POST("xapi/user") suspend fun updateUser(@Body u: User): Response<Unit>
    @DELETE("xapi/user") suspend fun deleteUser(@Query("name") name: String): Response<Unit>
}

object ApiProvider {
    fun create(baseUrl: String): TitanApi {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TitanApi::class.java)
    }
}
