package com.hmju.visual.ui.select

import kotlinx.coroutines.Deferred
import retrofit2.http.GET

/**
 * Description :
 *
 * Created by juhongmin on 2023/01/01
 */
interface GithubApiService {

    @GET("/sieunju/widget/develop/storage/selection.json")
    fun fetchSelectionAsync(): Deferred<SelectionResponse>
}