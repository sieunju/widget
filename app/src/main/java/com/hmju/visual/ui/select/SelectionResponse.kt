package com.hmju.visual.ui.select

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2023/01/01
 */
@Serializable
data class SelectionResponse(
    @SerialName("payload")
    val list : List<SelectionModel>
)