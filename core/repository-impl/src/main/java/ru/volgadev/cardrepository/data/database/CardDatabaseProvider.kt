package ru.volgadev.cardrepository.data.database

import android.content.Context

/**
 * Provider hides Room dependencies from internal module
 */
internal class CardDatabaseProvider {
    companion object {
        @JvmStatic
        fun createArticleDatabase(context: Context): CardDatabase =
            CardDatabaseImpl.getInstance(context)
    }
}