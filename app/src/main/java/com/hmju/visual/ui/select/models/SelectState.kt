package com.hmju.visual.ui.select.models

/**
 * Description : Selection State
 *
 * Created by juhongmin on 2025. 10. 5.
 */
sealed interface SelectState {

	object Loading : SelectState

	data class Contents(
		val list: List<Card>
	) : SelectState

	data class Error(val err: Throwable) : SelectState
}
