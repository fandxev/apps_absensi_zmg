package com.example.aplikasipresensizmg.helper.retrofit

import com.example.aplikasipresensizmg.BankURL
import com.example.aplikasipresensizmg.model.ChecklogModel
import com.example.aplikasipresensizmg.model.LoginModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiInterface {
    @FormUrlEncoded
    @POST(BankURL.URL_LOGIN_RETROFIT)
    fun login(@Field("nip") nip : String) : Call<LoginModel>

    @Multipart
    @POST("checklog")
    fun uploadTextAndFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part?,
        @Part("ssid") ssid: RequestBody?
    ): Call<ChecklogModel?>?

    //fun validasi apakah token masih aktif atau expired. apabila expired maka error 401(no LoginModel respon)
    @GET("validate-token")
    fun validateToken(
        @Header("Authorization") token:String,
    ): Call<LoginModel>

    @POST("logout")
    fun logout(
        @Header("Authorization") token:String,
    ): Call<LoginModel>

    companion object {
        var BASE_URL = BankURL.BASE_URL
        fun create() : ApiInterface {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(ApiInterface::class.java)
        }
    }
}