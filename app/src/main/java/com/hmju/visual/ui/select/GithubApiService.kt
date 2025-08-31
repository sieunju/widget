package com.hmju.visual.ui.select

import retrofit2.http.GET

/**
 * Description :
 *
 * Created by juhongmin on 2023/01/01
 */
interface GithubApiService {

    // https://api.github.com/repos/sieunju/widget/contents/storage?ref=develop
    @GET("/sieunju/widget/main/storage/selection.json")
    suspend fun fetchSelection(): SelectionResponseDTO
}