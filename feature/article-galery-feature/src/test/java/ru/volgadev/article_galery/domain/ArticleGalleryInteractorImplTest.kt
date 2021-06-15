package ru.volgadev.article_galery.domain

import com.nhaarman.mockito_kotlin.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.mockito.Mockito.`when`
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.core.musicplayer.api.MusicPlayer
import ru.volgadev.music_data.domain.MusicRepository
import java.util.*

/**
 * Test on [ArticleGalleryInteractorImpl]
 */
class ArticleGalleryInteractorImplTest {

    private val articleRepository = mock<ArticleRepository>()
    private val musicRepository = mock<MusicRepository>()
    private val musicPlayer = mock<MusicPlayer>()
    private val cardPlayer = mock<MusicPlayer>()

    @Test
    fun `when subscribe to availableCategories, then not paid items filtered`() = runBlockingTest {
        /* arrange */
        val freeCategory = buildRandomCategory(isPaid = false, marketItemId = null)
        val notPaidCategory = buildRandomCategory(isPaid = false, marketItemId = randomString())
        val paidCategory = buildRandomCategory(isPaid = true, marketItemId = randomString())
        val categoriesFlow = MutableStateFlow(
            listOf(
                freeCategory,
                notPaidCategory,
                paidCategory
            )
        )
        `when`(articleRepository.categories).thenReturn(categoriesFlow)

        /* action */
        val interactor = ArticleGalleryInteractorImpl(
            articleRepository,
            musicRepository,
            musicPlayer,
            cardPlayer,
            isBackgroundMusicEnabled = false
        )
        val availableCategories = interactor.availableCategories().first()

        /* assert */
        assert(availableCategories.contains(freeCategory))
        assert(availableCategories.contains(paidCategory))
        assert(!availableCategories.contains(notPaidCategory))
    }

    private fun randomString() = UUID.randomUUID().toString()

    private fun buildRandomCategory(marketItemId: String? = null, isPaid: Boolean) =
        ArticleCategory(
            id = randomString(),
            name = "",
            description = "",
            fileUrl = "",
            marketItemId = marketItemId,
            isPaid = isPaid,
            iconUrl = null
        )
}