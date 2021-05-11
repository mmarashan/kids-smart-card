package ru.volgadev.core.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Before
import org.junit.Test

@Suppress("UnstableApiUsage")
/**
 * Test on [SampleCodeDetector]
 */
class SampleCodeDetectorTest {

    private lateinit var task: TestLintTask

    @Before
    fun prepare() {
        task = lint().allowMissingSdk(true)
    }

    @Test
    fun testBasic() {
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
            .issues(SampleCodeDetector.ISSUE)
            .run()
            .expect(
                """
                    src/test/pkg/TestClass1.java:5: Warning: This code mentions lint: Congratulations [ShortUniqueId]
                        private static String s2 = "Let's say it: lint";
                                                   ~~~~~~~~~~~~~~~~~~~~
                    0 errors, 1 warnings
                    """
            )
    }
}