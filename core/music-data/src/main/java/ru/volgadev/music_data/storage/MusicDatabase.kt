package ru.volgadev.music_data.storage

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import ru.volgadev.music_data.model.MusicTrack
import ru.volgadev.music_data.model.MusicTrackType

@Dao
@WorkerThread
interface MusicTrackDao {
    @Query("SELECT * FROM musictrack")
    fun getAll(): List<MusicTrack>

    @Query("SELECT * FROM musictrack WHERE type = :type")
    fun getAllByType(type: MusicTrackType): List<MusicTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: MusicTrack)

    @Delete
    fun delete(user: MusicTrack)

    @Query("SELECT EXISTS(SELECT * FROM musictrack WHERE url = :url)")
    fun isExist(url : String) : Boolean

    @Query("SELECT filePath FROM musictrack WHERE url = :url")
    fun getPathByUrl(url : String) : String?

    @Query("SELECT * FROM musictrack WHERE url = :url")
    fun getByUrl(url : String) : MusicTrack?
}

@Database(entities = [MusicTrack::class], version = 1)
@TypeConverters(MusicTrackTypeConverter::class)
abstract class MusicTrackDatabase : RoomDatabase() {

    abstract fun dao(): MusicTrackDao

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
                MusicTrackDatabase::class.java, MUSIC_TRACK_DATABASE_NAME
            ).build()
    }
}

private class MusicTrackTypeConverter {

    @TypeConverter
    fun from(type: MusicTrackType): String {
        return type.name
    }

    @TypeConverter
    fun to(data: String): MusicTrackType {
        return MusicTrackType.valueOf(data)
    }
}