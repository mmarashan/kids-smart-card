package ru.volgadev.music_data.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.MusicTrackDao
import ru.volgadev.music_data.domain.MusicTrackDatabase
import ru.volgadev.music_data.domain.model.MusicTrackType

@Database(entities = [MusicTrack::class], version = 1)
@TypeConverters(MusicTrackTypeConverter::class)
internal abstract class MusicTrackDatabaseImpl : MusicTrackDatabase, RoomDatabase() {

    abstract override fun dao(): MusicTrackDao

    companion object {
        @Volatile
        private var INSTANCE: MusicTrackDatabase? = null

        private const val MUSIC_TRACK_DATABASE_NAME = "music-database.db"

        fun getInstance(context: Context): MusicTrackDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                MusicTrackDatabaseImpl::class.java, MUSIC_TRACK_DATABASE_NAME
            ).build()
    }
}

internal class MusicTrackTypeConverter {

    @TypeConverter
    fun from(type: MusicTrackType): String {
        return type.name
    }

    @TypeConverter
    fun to(data: String): MusicTrackType {
        return MusicTrackType.valueOf(data)
    }
}