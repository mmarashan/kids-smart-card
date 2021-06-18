package ru.volgadev.papastory.ui

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.volgadev.common.ext.hideNavBar
import ru.volgadev.common.ext.isPermissionGranted
import ru.volgadev.common.log.Logger
import ru.volgadev.papastory.R
import ru.volgadev.papastory.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private val logger = Logger.get("MainActivity")

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug("onCreate($savedInstanceState)")
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestPermission = neededPermissions.map { isPermissionGranted(it) }.contains(false)
        if (requestPermission) {
            showPermissionAlertDialog {
                ActivityCompat.requestPermissions(this, neededPermissions, 0)
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.currentScreen.collect {
                showFragment(it.getScreen())
                hideNavBar()
            }
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    viewModel.onOpenScreenIntent(CabinetScreen)
                    true
                }
                R.id.action_galery -> {
                    viewModel.onOpenScreenIntent(CardsScreen)
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.action_galery
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

    private fun showPermissionAlertDialog(onClose: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(R.string.request_storage_permission_text)
            .setPositiveButton(android.R.string.yes) { _, _ -> onClose.invoke() }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private companion object {
        const val CONTENT_CONTAINER_ID = R.id.contentContainer

        val neededPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}