package ru.volgadev.article_galery.presentation

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import ru.volgadev.article_galery.domain.ArticleGalleryInteractor
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.test.MainCoroutineRule
import kotlin.test.assertEquals

/**
 * Test on [ArticleGalleryViewModel]
 */
class ArticleGalleryViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val interactor = mockk<ArticleGalleryInteractor>()

    @Test
    fun `when onClickCategory, then new articles emit`() = runBlockingTest {
        /* arrange */
        val category1 =
            ArticleCategory(
                id = "1",
                name = "",
                description = "",
                fileUrl = "/",
                isPaid = true,
                iconUrl = null,
                marketItemId = null
            )
        val category2 =
            ArticleCategory(
                id = "2",
                name = "",
                description = "",
                fileUrl = "/",
                isPaid = true,
                iconUrl = null,
                marketItemId = null
            )

        val listArticles1 = listOf(
            Article(
                id = 1L,
                author = "",
                title = "",
                categoryId = "1",
                tags = emptyList(),
                iconUrl = null,
                openPhrase = null,
                onClickSounds = emptyList()
            )
        )
        val listArticles2 = listOf(
            Article(
                id = 2L,
                author = "",
                title = "",
                categoryId = "2",
                tags = emptyList(),
                iconUrl = null,
                openPhrase = null,
                onClickSounds = emptyList()
            )
        )

        val categories = listOf(category1, category2)

        every { interactor.availableCategories() } returns MutableStateFlow(categories)
        coEvery { interactor.getCategoryArticles(category1) } returns listArticles1
        coEvery { interactor.getCategoryArticles(category2) } returns listArticles2

        val viewModel = ArticleGalleryViewModel(interactor)

        /* action */
        viewModel.onClickCategory(category1)
        val emitedCategory = viewModel.currentCategory.first()
        val emitedArticles = viewModel.currentArticles.first()

        /* checking */
        assertEquals(category1.id, emitedCategory.id)
        assertEquals(listArticles1.size, emitedArticles.size)
        assertEquals(listArticles1.first(), emitedArticles.first())
    }
}