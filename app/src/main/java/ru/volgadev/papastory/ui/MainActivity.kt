package ru.volgadev.papastory.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.main_activity.*
import ru.volgadev.papastory.R
import ru.volgadev.article_galery.ui.ArticleGalleryFragment
import ru.volgadev.article_page.ArticlePageFragment
import ru.volgadev.article_page.ITEM_ID_KEY
import ru.volgadev.common.hideNavBar
import ru.volgadev.common.log.Logger

const val HOME_ITEM_ID = R.id.action_home
const val GALERY_ITEM_ID = R.id.action_galery

class MainActivity : AppCompatActivity() {

    private val logger = Logger.get("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("On create")
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.hideNavBar()

        val galleryFragment: ArticleGalleryFragment =
            FragmentProvider.get(AppFragment.GALERY_FRAGMENT) as ArticleGalleryFragment
        galleryFragment.setOnItemClickListener(object : ArticleGalleryFragment.OnItemClickListener {
            override fun onClick(itemId: Long, clickedView: View) {
                logger.debug("Choose $itemId item to show")
                val itemPageFragment =
                    FragmentProvider.get(AppFragment.ARTICLE_PAGE_FRAGMENT) as ArticlePageFragment
                itemPageFragment.enterTransition = Fade()
                showFragment(
                    itemPageFragment,
                    Bundle().apply { putLong(ITEM_ID_KEY, itemId) },
                    true,
                    clickedView
                )
            }
        })

        bottomNavigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {

            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    HOME_ITEM_ID -> {
                        Toast.makeText(
                            applicationContext,
                            "TODO: On click home",
                            Toast.LENGTH_SHORT
                        ).show()
                        return true
                    }
                    GALERY_ITEM_ID -> {
                        showFragment(galleryFragment)
                        return true
                    }
                }
                return false
            }
        })
        bottomNavigation.selectedItemId = GALERY_ITEM_ID
    }

    private fun showFragment(
        fragment: Fragment,
        arguments: Bundle? = null,
        addToBackStack: Boolean = false,
        clickedView: View? = null
    ) {
        logger.debug("Show fragment ${fragment.javaClass.canonicalName}")
        fragment.arguments = arguments
        if (FragmentProvider.isFullscreen(fragment)) {
            hideNavigationPanel()
        } else {
            showNavigationPanel()
        }

        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, fragment)

        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun showNavigationPanel() {
        bottomNavigation.visibility = View.VISIBLE
    }

    private fun hideNavigationPanel() {
        bottomNavigation.visibility = View.GONE
    }
}