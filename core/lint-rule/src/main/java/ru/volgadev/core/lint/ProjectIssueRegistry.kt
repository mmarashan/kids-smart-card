package ru.volgadev.core.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

/*
 * The list of issues that will be checked when running <code>lint</code>.
 */
@Suppress("UnstableApiUsage")
class ProjectIssueRegistry : IssueRegistry() {
    override val issues = listOf(GlobalScopeDetector.ISSUE)

    override val api: Int
        get() = CURRENT_API
}