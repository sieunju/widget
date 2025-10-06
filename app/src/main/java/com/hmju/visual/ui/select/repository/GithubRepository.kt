package com.hmju.visual.ui.select.repository

import android.content.Context
import com.hmju.visual.R
import com.hmju.visual.ui.select.apis.GithubApiService
import com.hmju.visual.ui.select.dto.CardDTO
import com.hmju.visual.ui.select.dto.GithubContentsDTO
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import hmju.http.tracking_interceptor.TrackingHttpInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import java.io.File
import java.net.SocketTimeoutException

/**
 * Description : Github REST API
 *
 * Created by juhongmin on 2025. 10. 4.
 */
internal class GithubRepository(
    private val context: Context
) {
    private val baseUrl = HttpUrl.Builder()
        .host("api.github.com")
        .scheme("https")
        .addPathSegments("repos/sieunju/widget/contents/")
        .build()
    private val cache = Cache(
        directory = File(context.cacheDir, "example_cache"),
        maxSize = 50L * 1024 * 1024 // 50MB
    )
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(TrackingHttpInterceptor())
        .cache(cache)
        .build()
    private val json: Json = Json {
        isLenient = true // Json 큰따옴표 느슨하게 체크.
        ignoreUnknownKeys = true // Field 값이 없는 경우 무시
        coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
    }

    @ExperimentalSerializationApi
    private val jsonConverter: Converter.Factory by lazy {
        json.asConverterFactory("application/json".toMediaType())
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val apiService: GithubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonConverter)
            .client(httpClient)
            .build()
            .create()
    }

    private suspend fun <T> customTimeout(
        timeoutMillis: Long = 10 * 1000,
        block: suspend () -> T
    ): T {
        return withTimeoutOrNull(timeoutMillis) {
            withContext(Dispatchers.IO) { block() }
        } ?: throw SocketTimeoutException("Request timeout after ${timeoutMillis}ms")
    }

    suspend fun fetchAssets(): List<GithubContentsDTO> {
        return customTimeout {
            val res = apiService.fetchAssets(context.getString(R.string.git_branch)).body()
                ?: throw NullPointerException("Response is Null")
            return@customTimeout res
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun downloadSelectionJson(url: String): List<CardDTO> {
        return customTimeout {
            val jsonString = apiService.downloadFile(url).body()!!.string()
            val data = json.decodeFromString<List<CardDTO>>(jsonString)
            return@customTimeout data
        }
    }

    suspend fun downloadFile(url: String): ByteArray {
        return customTimeout {
            return@customTimeout apiService.downloadFile(url).body()!!.bytes()
        }
    }
}