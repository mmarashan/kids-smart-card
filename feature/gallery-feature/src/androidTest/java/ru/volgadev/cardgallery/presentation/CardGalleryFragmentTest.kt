package ru.volgadev.cardgallery.presentation

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.volgadev.cardgallery.R
import ru.volgadev.cardgallery.domain.ArticleGalleryInteractor
import ru.volgadev.cardgallery.presentation.adapter.TagViewHolder
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory
import ru.volgadev.common.test.FragmentTestRule
import java.lang.Thread.sleep

/**
 * UI test on [CardGalleryFragment]
 */
class CardGalleryFragmentTest {

    @get:Rule
    val fragmentTestRule: FragmentTestRule<CardGalleryFragment> =
        FragmentTestRule(CardGalleryFragment::class.java)

    private val interactor = mockk<ArticleGalleryInteractor>(relaxed = true)

    private companion object {
        var koinStarted = false
    }

    @Before
    fun setup() {
        if (koinStarted) return
        startKoin {
            modules(module {
                viewModel {
                    CardGalleryViewModel(interactor)
                }
            })
            koinStarted = true
        }
    }

    @Test
    fun checkCategoryRecyclerViewVisible() {
        /* action */
        fragmentTestRule.launchActivity(null)

        /* assert */
        onView(withId(R.id.categoryRecyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun checkViewModelWhenCardClicked() {
        /* arrange */
        val category = CardCategory(
            id = "1",
            name = "",
            description = "",
            fileUrl = "/",
            isPaid = true,
            iconUrl = null,
            marketItemId = null
        )

        every { interactor.availableCategories() } returns MutableStateFlow(listOf(category))
        coEvery { interactor.getCategoryArticles(category) } returns emptyList()

        /* action */
        fragmentTestRule.launchActivity(null)
        sleep(3000L)
        onView(withId(R.id.categoryRecyclerView)).perform(
            actionOnItemAtPosition<TagViewHolder>(
                0,
                click()
            )
        );

        /* assert */
        onView(withId(R.id.categoryRecyclerView)).check(matches(isDisplayed()))
    }
}