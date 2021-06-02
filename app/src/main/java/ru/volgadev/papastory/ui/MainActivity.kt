package ru.volgadev.papastory.ui

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ru.volgadev.common.hideNavBar
import ru.volgadev.common.isPermissionGranted
import ru.volgadev.common.log.Logger
import ru.volgadev.common.setVisibleWithTransition
import ru.volgadev.papastory.R
import ru.volgadev.papastory.databinding.MainActivityBinding

const val CONTENT_CONTAINER_ID = R.id.contentContainer

private val neededPermissions = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

private const val ENTER_FRAGMENT_TRANSITION_DURATION_MS = 600L
private const val EXIT_FRAGMENT_TRANSITION_DURATION_MS = 600L

class MainActivity : AppCompatActivity() {

    private val logger = Logger.get("MainActivity")

    private val fragmentProvider = FragmentFeatureProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("onCreate($savedInstanceState)")
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val needRequestPermission =
            neededPermissions.map { isPermissionGranted(it) }.contains(false)

        if (needRequestPermission) {
            showPermissionAlertDialog {
                ActivityCompat.requestPermissions(this, neededPermissions, 0)
            }
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    showCabinet()
                    true
                }
                R.id.action_galery -> {
                    showGallery()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.action_galery

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.contentContainer)
            currentFragment?.let { provideNavigationPanelVisibility(binding, it) }
        }
    }

    private fun showCabinet() {
        val cabinetFragment = fragmentProvider.getFragment(AppFragment.CABINET_FRAGMENT)
        showFragment(cabinetFragment)
    }

    private fun showGallery() {
        val galleryFragment = (fragmentProvider.getFragment(AppFragment.GALLERY_FRAGMENT))
        showFragment(galleryFragment)
    }

    override fun onResume() {
        logger.debug("onResume()")
        hideNavBar()
        super.onResume()
    }

    private fun showFragment(
        fragment: Fragment,
        addToBackStack: Boolean = false
    ) {
        logger.debug("Show fragment ${fragment.javaClass.canonicalName}")
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(CONTENT_CONTAINER_ID, fragment, null)
        if (addToBackStack) transaction.addToBackStack(fragment.javaClass.name)
        transaction.commit()
    }

    private fun provideNavigationPanelVisibility(binding: MainActivityBinding, fragment: Fragment) {
        if (FragmentFeatureProvider.isFullscreen(fragment)) {
            binding.hideBottomNavigationPanel()
        } else {
            binding.showBottomNavigationPanel()
        }
    }

    private fun MainActivityBinding.showBottomNavigationPanel() =
        bottomNavigation.setVisibleWithTransition(
            View.VISIBLE,
            Slide(Gravity.BOTTOM),
            ENTER_FRAGMENT_TRANSITION_DURATION_MS,
            root
        )

    private fun MainActivityBinding.hideBottomNavigationPanel() =
        bottomNavigation.setVisibleWithTransition(
            View.GONE,
            Slide(Gravity.BOTTOM),
            EXIT_FRAGMENT_TRANSITION_DURATION_MS,
            root
        )

    private fun showPermissionAlertDialog(onClose: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(R.string.request_storage_permission_text)
            .setPositiveButton(android.R.string.yes) { _, _ -> onClose.invoke() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}