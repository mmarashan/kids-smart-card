package ru.volgadev.core.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Before
import org.junit.Test

/**
 * Test on [GlobalScopeDetector]
 */
@Suppress("UnstableApiUsage")
class GlobalScopeDetectorTest {

    private lateinit var task: TestLintTask

    @Before
    fun prepare() {
        task = lint().allowMissingSdk(true)
    }

    @Test
    fun checkFoundGlobalScope() {
        task.files(
            kotlin(
                """
                    package test.pkg
                    class TestClass1 {
                        
                        init {
                           GlobalScope.launch {  }
                        }
                    }
                    """
            ).indented()
        )
            .issues(GlobalScopeDetector.ISSUE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.kt:5: Warning: Don't use GlobalScope!!! [GlobalScopeWarningId]
                           GlobalScope.launch {  }
                           ~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
            )
    }
}