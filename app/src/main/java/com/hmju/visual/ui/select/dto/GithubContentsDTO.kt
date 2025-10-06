package com.hmju.visual.ui.select.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2025. 10. 4.
 */
@Serializable
internal data class GithubContentsDTO(
    val name: String = "",
    val type: String = "",
    @SerialName("download_url")
    val downloadUrl: String
)
