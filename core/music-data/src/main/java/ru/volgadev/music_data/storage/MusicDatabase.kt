package ru.volgadev.music_data.storage

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import ru.volgadev.music_data.model.MusicTrack

@Dao
@WorkerThread
interface MusicTrackDao {
    @Query("SELECT * FROM musictrack")
    fun getAll(): List<MusicTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: MusicTrack)

    @Delete
    fun delete(user: MusicTrack)

    @Query("SELECT EXISTS(SELECT * FROM musictrack WHERE url = :url)")
    fun isExist(url : String) : Boolean
}

@Database(entities = [MusicTrack::class], version = 1)
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