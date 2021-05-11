package ru.volgadev.core.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.*

@Suppress("UnstableApiUsage")
class SampleCodeDetector : Detector(), UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun visitExpression(node: UExpression) {
                val string = node.evaluateString().orEmpty()
                if (string.contains("GlobalScope")) {
                    context.report(
                        ISSUE, node, context.getLocation(node),
                        "Don't use GlobalScope!!!"
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Issue describing the problem and pointing to the detector
         * implementation.
         */
        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "GlobalScopeWarningId",
            briefDescription = "GlobalScope usage warning",
            explanation = """
                    You shouldn't use GlobalScope! Instead of use a custom scope.
                    """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                SampleCodeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}