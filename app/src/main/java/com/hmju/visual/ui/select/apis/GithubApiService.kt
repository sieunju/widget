package com.hmju.visual.ui.select.apis

import com.hmju.visual.ui.select.dto.GithubContentsDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Description : Github REST API Service
 * https://api.github.com/repos/sieunju/widget/contents/storage?ref=develop
 * Created by juhongmin on 2023/01/01
 */
internal interface GithubApiService {

    @GET("assets")
    suspend fun fetchAssets(
        @Query("ref") branch: String
    ): Response<List<GithubContentsDTO>>

    @GET
    suspend fun downloadFile(
        @Url fileUrl: String
    ): Response<ResponseBody>
}