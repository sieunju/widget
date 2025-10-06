package com.hmju.visual.ui.select

import com.hmju.visual.ui.select.dto.CardDTO
import com.hmju.visual.ui.select.dto.CardDTO.Companion.contentsName
import com.hmju.visual.ui.select.models.Card
import com.hmju.visual.ui.select.models.SelectState
import com.hmju.visual.ui.select.repository.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Description :
 *
 * Created by juhongmin on 2025. 10. 6.
 */
internal class SelectionUseCase(
    private val repository: GithubRepository
) {
    operator fun invoke(): Flow<SelectState> {
        return flow {
            emit(SelectState.Loading)
            val selection = repository.fetchAssets()
                .find { it.name == "selection.json" }
                ?.let { repository.downloadSelectionJson(it.downloadUrl) }
            if (selection == null) {
                emit(SelectState.Error(NullPointerException("Selection json is Null")))
                return@flow
            }

            val contents = coroutineScope {
                selection.map { async { it.toCard() } }.awaitAll().filterNotNull()
            }
            emit(SelectState.Contents(contents))
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun <T> customTimeout(
        timeoutMillis: Long = 10 * 1000,
        block: suspend () -> T
    ): T? {
        return withTimeoutOrNull(timeoutMillis) {
            withContext(Dispatchers.IO) { block() }
        }
    }

    private suspend fun CardDTO.toCard(): Card? {
        return withContext(Dispatchers.IO) {
            val downloadUrl = repository.fetchAssets()
                .find { it.name == contentsName() }?.downloadUrl
                ?: return@withContext null
            val contents = if (contentsName().endsWith(".webp")) {
                downloadWebP(downloadUrl)
            } else {
                downloadImage(downloadUrl)
            }
            if (contents == null) return@withContext null
            return@withContext when (this@toCard) {
                is CardDTO.FragmentCardDTO -> Card.FragmentCard(
                    title = title,
                    contents = contents,
                    router = router
                )

                is CardDTO.ActivityCardDTO -> Card.ActivityCard(
                    title = title,
                    contents = contents,
                    router = router
                )
            }
        }
    }

    private suspend fun downloadWebP(downloadUrl: String): Card.ContentsType.WebP? {
        return customTimeout {
            try {
                val bytes = repository.downloadFile(downloadUrl)
                Card.ContentsType.WebP(
                    name = downloadUrl,
                    bytes = bytes
                )
            } catch (ex: Exception) {
                null
            }
        }
    }

    private suspend fun downloadImage(downloadUrl: String): Card.ContentsType.Images? {
        return customTimeout {
            try {
                val bytes = repository.downloadFile(downloadUrl)
                Card.ContentsType.Images(
                    name = downloadUrl,
                    bytes = bytes
                )
            } catch (ex: Exception) {
                null
            }
        }
    }
}