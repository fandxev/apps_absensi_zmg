package com.example.aplikasipresensizmg.helper.retrofit

import com.example.aplikasipresensizmg.BankURL
import com.example.aplikasipresensizmg.model.LoginModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ApiInterface {
    @FormUrlEncoded
    @POST(BankURL.URL_LOGIN_RETROFIT)
    fun login(@Field("nip") nip : String) : Call<LoginModel>



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