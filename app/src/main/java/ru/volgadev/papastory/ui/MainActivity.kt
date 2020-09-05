package ru.volgadev.papastory.ui

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.main_activity.*
import ru.volgadev.article_galery.ui.ArticleGalleryFragment
import ru.volgadev.article_page.ArticlePageFragment
import ru.volgadev.article_page.ITEM_ID_KEY
import ru.volgadev.common.hideNavBar
import ru.volgadev.common.isPermissionGranted
import ru.volgadev.common.log.Logger
import ru.volgadev.common.setVisibleWithTransition
import ru.volgadev.papastory.R

const val HOME_ITEM_ID = R.id.action_home
const val GALERY_ITEM_ID = R.id.action_galery
const val CONTENT_CONTAINER_ID = R.id.contentContainer

private val NEEDED_PERMISSIONS = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)
private const val REQUEST_CODE = 123

class MainActivity : AppCompatActivity() {

    private val logger = Logger.get("MainActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("onCreate($savedInstanceState)")
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        this.hideNavBar()

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
            FragmentProvider.get(AppFragment.GALERY_FRAGMENT) as ArticleGalleryFragment
        galleryFragment.enterTransition = Slide(Gravity.END)
        galleryFragment.exitTransition = Slide(Gravity.START)
        galleryFragment.setOnItemClickListener(object : ArticleGalleryFragment.OnItemClickListener {
            override fun onClick(itemId: Long, clickedView: View) {
                logger.debug("Choose $itemId item to show")
                val itemPageFragment =
                    FragmentProvider.get(AppFragment.ARTICLE_PAGE_FRAGMENT) as ArticlePageFragment
                itemPageFragment.enterTransition = Slide(Gravity.START)
                itemPageFragment.exitTransition = Slide(Gravity.END)
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
                        logger.debug("HOME_ITEM_ID selected")
                        Toast.makeText(
                            applicationContext,
                            "TODO: On click home",
                            Toast.LENGTH_SHORT
                        ).show()
                        return true
                    }
                    GALERY_ITEM_ID -> {
                        logger.debug("GALERY_ITEM_ID selected")
                        if (savedInstanceState == null) {
                            showFragment(galleryFragment)
                        }
                        return true
                    }
                }
                return false
            }
        })
        bottomNavigation.selectedItemId = GALERY_ITEM_ID

        supportFragmentManager.addOnBackStackChangedListener {
            supportFragmentManager.findFragmentById(
                R.id.contentContainer
            )?.let { checkHideNavigationPanel(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        logger.debug("onResume()")
        hideNavBar()
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

    private fun checkHideNavigationPanel(fragment: Fragment){
        if (FragmentProvider.isFullscreen(fragment)) {
            hideNavigationPanel()
        } else {
            showNavigationPanel()
        }
    }

    private fun showNavigationPanel() {
        bottomNavigation.setVisibleWithTransition(
            View.VISIBLE,
            Slide(Gravity.BOTTOM), 600, mainActivityLayout
        )
    }

    private fun hideNavigationPanel() {
        bottomNavigation.setVisibleWithTransition(
            View.GONE,
            Slide(Gravity.BOTTOM), 600, mainActivityLayout
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