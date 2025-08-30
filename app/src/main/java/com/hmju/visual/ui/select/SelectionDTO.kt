package com.hmju.visual.ui.select

import kotlinx.serialization.Serializable

@Serializable
data class SelectionDTO(
    val title: String = "",
    val imageUrl: String = "",
    val fragmentName: String = "",
    val activityName: String = ""
)