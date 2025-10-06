package com.hmju.visual.ui.select.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2025. 10. 4.
 */
@Serializable
sealed class CardDTO {

    abstract val type: String

    @SerialName("fragment")
    @Serializable
    data class FragmentCardDTO(
        override val type: String = "fragment",
        val title: String = "",
        @SerialName("contentsName")
        val contentsName: String = "",
        @SerialName("routerName")
        val router: String = ""
    ) : CardDTO()

    @SerialName("activity")
    @Serializable
    data class ActivityCardDTO(
        override val type: String = "activity",
        val title: String = "",
        @SerialName("contentsName")
        val contentsName: String = "",
        @SerialName("routerName")
        val router: String = ""
    ) : CardDTO()

    companion object {
        fun CardDTO.contentsName(): String {
            return when (this) {
                is FragmentCardDTO -> contentsName
                is ActivityCardDTO -> contentsName
            }
        }
    }
}