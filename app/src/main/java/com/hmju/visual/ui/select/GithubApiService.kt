package com.hmju.visual.ui.select

import retrofit2.http.GET

/**
 * Description :
 *
 * Created by juhongmin on 2023/01/01
 */
interface GithubApiService {

    @GET("/stroage/selection.json")
    fun fetchSelected()
}