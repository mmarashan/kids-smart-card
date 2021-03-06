package ru.volgadev.papastory.ui

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleType
import ru.volgadev.article_galery.ui.ArticleGalleryFragment
import ru.volgadev.article_page.ArticlePageFragment
import ru.volgadev.article_page.ITEM_ID_KEY
import ru.volgadev.common.hideNavBar
import ru.volgadev.common.isPermissionGranted
import ru.volgadev.common.log.Logger
import ru.volgadev.common.setVisibleWithTransition
import ru.volgadev.papastory.R

const val HOME_ITEM_ID = R.id.action_home
const val GALLERY_ITEM_ID = R.id.action_galery
const val CONTENT_CONTAINER_ID = R.id.contentContainer

private val NEEDED_PERMISSIONS = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)
private const val REQUEST_CODE = 123

private const val ENTER_FRAGMENT_TRANSITION_DURATION_MS = 600L
private const val EXIT_FRAGMENT_TRANSITION_DURATION_MS = 600L

class MainActivity : AppCompatActivity() {

    private val logger = Logger.get("MainActivity")

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("onCreate($savedInstanceState)")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val needRequestPermission =
            NEEDED_PERMISSIONS.map { permission -> this.isPermissionGranted(permission) }
                .contains(false)
        if (needRequestPermission) {
            showPermissionAlertDialog {
                ActivityCompat.requestPermissions(
                    this,
                    NEEDED_PERMISSIONS,
                    REQUEST_CODE
                )
            }
        }

        val galleryFragment: ArticleGalleryFragment =
            FragmentProvider.get(AppFragment.GALLERY_FRAGMENT) as ArticleGalleryFragment
        galleryFragment.run {
            enterTransition = Slide(Gravity.END).apply {
                duration = ENTER_FRAGMENT_TRANSITION_DURATION_MS
            }
            exitTransition = Slide(Gravity.START).apply {
                duration = EXIT_FRAGMENT_TRANSITION_DURATION_MS
            }
            setOnItemClickListener(object : ArticleGalleryFragment.OnItemClickListener {
                override fun onClick(article: Article, clickedView: View) {
                    logger.debug("Choose ${article.title} to show")
                    val itemPageFragment =
                        FragmentProvider.get(AppFragment.ARTICLE_PAGE_FRAGMENT) as ArticlePageFragment
                    itemPageFragment.exitTransition = Fade().apply {
                        duration = EXIT_FRAGMENT_TRANSITION_DURATION_MS
                    }
                    if (article.type != ArticleType.NO_PAGES) {
                        showFragment(
                            itemPageFragment,
                            Bundle().apply { putLong(ITEM_ID_KEY, article.id) },
                            true,
                            clickedView
                        )
                    }
                }
            })
        }

        val cabinetFragment = FragmentProvider.get(AppFragment.CABINET_FRAGMENT).apply {
            enterTransition = Slide(Gravity.START).apply {
                duration = ENTER_FRAGMENT_TRANSITION_DURATION_MS
            }
            exitTransition = Slide(Gravity.END).apply {
                duration = EXIT_FRAGMENT_TRANSITION_DURATION_MS
            }
        }

        bottomNavigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    HOME_ITEM_ID -> {
                        logger.debug("Show gallery fragment")
                        showFragment(cabinetFragment)
                        return true
                    }
                    GALLERY_ITEM_ID -> {
                        logger.debug("galleryFragment selected")
                        showFragment(galleryFragment)
                        return true
                    }
                }
                return false
            }
        })
        bottomNavigation.selectedItemId = GALLERY_ITEM_ID

        supportFragmentManager.addOnBackStackChangedListener {
            supportFragmentManager.findFragmentById(
                R.id.contentContainer
            )?.let { provideNavigationPanelVisibility(it) }
        }
    }

    override fun onResume() {
        logger.debug("onResume()")
        hideNavBar()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        logger.debug("onPause()")
    }

    private fun showFragment(
        fragment: Fragment,
        arguments: Bundle? = null,
        addToBackStack: Boolean = false,
        clickedView: View? = null
    ) {
        logger.debug("Show fragment ${fragment.javaClass.canonicalName}")
        fragment.arguments = arguments
        val tagFragment = fragment.javaClass.canonicalName
        val transaction = supportFragmentManager.beginTransaction()
            .replace(CONTENT_CONTAINER_ID, fragment, tagFragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun provideNavigationPanelVisibility(fragment: Fragment) {
        if (FragmentProvider.isFullscreen(fragment)) {
            hideBottomNavigationPanel()
        } else {
            showBottomNavigationPanel()
        }
    }

    private fun showBottomNavigationPanel() {
        logger.debug("showBottomNavigationPanel()")
        bottomNavigation.setVisibleWithTransition(
            View.VISIBLE,
            Slide(Gravity.BOTTOM), ENTER_FRAGMENT_TRANSITION_DURATION_MS, mainActivityLayout
        )
    }

    private fun hideBottomNavigationPanel() {
        logger.debug("hideBottomNavigationPanel()")
        bottomNavigation.setVisibleWithTransition(
            View.GONE,
            Slide(Gravity.BOTTOM), EXIT_FRAGMENT_TRANSITION_DURATION_MS, mainActivityLayout
        )
    }

    @MainThread
    private fun showPermissionAlertDialog(onClose: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(R.string.request_storage_permission_text)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                onClose.invoke()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}