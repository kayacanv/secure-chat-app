package com.example.messengerapp

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class RetrofitInstance{

  companion object {
    val retrofit: ApiService by lazy {
      val httpClient = OkHttpClient.Builder()
      val builder = Retrofit.Builder()
          .baseUrl("http://138.68.111.102:5000/")
          .addConverterFactory(ScalarsConverterFactory.create())
          .addConverterFactory(GsonConverterFactory.create())

      val retrofit = builder
          .client(httpClient.build())
          .build()
      retrofit.create(ApiService::class.java)
    }
  }

}