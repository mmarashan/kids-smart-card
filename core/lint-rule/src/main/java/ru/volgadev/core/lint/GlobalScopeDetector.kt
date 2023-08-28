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

/**
 * Detect and warn using of coroutines GlobalScope in project
 */
@Suppress("UnstableApiUsage")
class GlobalScopeDetector : Detector(), UastScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(USimpleNameReferenceExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun  visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression){
                if (node.identifier.contains("GlobalScope")) {
                    context.report(
                        ISSUE, node, context.getLocation(node),
                        "Don't use GlobalScope!!!"
                    )
                }
            }
        }
    }

    companion object {
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
                GlobalScopeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}