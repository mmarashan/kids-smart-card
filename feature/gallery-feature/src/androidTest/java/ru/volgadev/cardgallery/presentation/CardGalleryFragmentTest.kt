package ru.volgadev.cardgallery.presentation

import ru.volgadev.cardgallery.R
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Rule
import org.junit.Test
import ru.volgadev.common.test.FragmentTestRule

/**
 * UI test on [CardGalleryFragment]
 */
class CardGalleryFragmentTest {

    @get:Rule
    val fragmentTestRule: FragmentTestRule<CardGalleryFragment> =
        FragmentTestRule(CardGalleryFragment::class.java)

    @Test
    fun checkCategoryRecyclerViewVisible() {

        /* action */
        fragmentTestRule.launchActivity(null)

        /* assert */
        onView(withId(R.id.categoryRecyclerView)).check(matches(isDisplayed()))
    }
}