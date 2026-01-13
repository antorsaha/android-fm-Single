package com.saha.androidfm.di

import android.util.Log
import com.google.gson.GsonBuilder
import com.saha.androidfm.BuildConfig
import com.saha.androidfm.data.network.MyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun providesMyApi(): MyApi {
        val interceptor = run {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.apply {
                if (BuildConfig.DEBUG) {
                    //Log.d("token", "providesMyApi: token ${BuildConfig.REPLICATE_API_KEY}")
                    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                }
            }
        }

        val okHttpClient: OkHttpClient =
            OkHttpClient.Builder().connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS).writeTimeout(300, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor)
                .addInterceptor { chain ->
                    val original: Request = chain.request()
                    val requestBuilder: Request.Builder =
                        original.newBuilder().addHeader("Connection", "keep-alive")
                            //.addHeader("Authorization", "Bearer ${BuildConfig.REPLICATE_API_KEY}")
                            .addHeader("Accept", "*/*")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Accept-Language", "en")


                    requestBuilder.method(original.method, original.body)
                    val request: Request = requestBuilder.build()

                    chain.proceed(request)
                }
                //.addInterceptor(SessionInterceptor(apiService))

                .build()

        val gsonBuilder = GsonBuilder()
        gsonBuilder.setLenient()
        val gson = gsonBuilder.create()

        return Retrofit.Builder().baseUrl("https://api.replicate.com/")
            .addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build()
            .create(MyApi::class.java)
    }
}