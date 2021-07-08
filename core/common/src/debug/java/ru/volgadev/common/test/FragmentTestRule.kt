package ru.volgadev.common.test

import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import ru.volgadev.common.R

/**
 * This rule is for testing fragment with Espresso
 * Copy from https://stackoverflow.com/questions/33647135/android-independent-fragment-ui-testing-tool
 */
class FragmentTestRule<F : Fragment?>(private val fragmentClass: Class<F>) :
    ActivityTestRule<TestActivity>(TestActivity::class.java, true, false) {
    var fragment: F? = null
        private set

    override fun afterActivityLaunched() {
        super.afterActivityLaunched()
        activity?.runOnUiThread {
            try {
                /* Instantiate and insert the fragment into the container layout */
                val manager: FragmentManager = activity!!.supportFragmentManager
                val transaction: FragmentTransaction = manager.beginTransaction()
                fragmentClass.newInstance()?.let {
                    fragment = it
                    transaction.replace(R.id.container, it)
                    transaction.commit()
                }
            } catch (e: InstantiationException) {
                Assert.fail(
                    "${javaClass.simpleName}: Could not insert ${fragmentClass.simpleName} into TestActivity: ${e.message}"
                )
            } catch (e: IllegalAccessException) {
                Assert.fail(
                    "${javaClass.simpleName}: Could not insert ${fragmentClass.simpleName} into TestActivity: ${e.message}"
                )
            }
        }
    }
}

@VisibleForTesting
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val frameLayout = FrameLayout(this)
        frameLayout.id = R.id.container
        setContentView(frameLayout)
    }
}