package ru.volgadev.cardgallery.presentation

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import ru.volgadev.cardgallery.domain.ArticleGalleryInteractor
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory
import ru.volgadev.common.test.MainCoroutineRule
import kotlin.test.assertEquals

/**
 * Test on [CardGalleryViewModel]
 */
class CardGalleryViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val interactor = mockk<ArticleGalleryInteractor>()

    @Test
    fun `when onClickCategory, then new articles emit`() = runBlockingTest {
        /* arrange */
        val category1 =
            CardCategory(
                id = "1",
                name = "",
                description = "",
                fileUrl = "/",
                isPaid = true,
                iconUrl = null,
                marketItemId = null
            )
        val category2 =
            CardCategory(
                id = "2",
                name = "",
                description = "",
                fileUrl = "/",
                isPaid = true,
                iconUrl = null,
                marketItemId = null
            )

        val listArticles1 = listOf(
            Card(
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
            Card(
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

        val viewModel = CardGalleryViewModel(interactor)

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