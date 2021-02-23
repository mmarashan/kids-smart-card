package ru.volgadev.music_data.data

import android.content.Context
import ru.volgadev.music_data.domain.MusicTrackDatabase

/**
 * Provider hides Room dependencies from internal module
 */
class MusicDatabaseProvider {
    companion object {
        fun createDatabase(context: Context): MusicTrackDatabase = MusicTrackDatabaseImpl.getInstance(context)
    }
}