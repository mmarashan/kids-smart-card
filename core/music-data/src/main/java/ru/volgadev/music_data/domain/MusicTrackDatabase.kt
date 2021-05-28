package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType

internal interface MusicTrackDatabase {
    fun dao(): MusicTrackDao
}

@Dao
@WorkerThread
internal interface MusicTrackDao {
    @Query("SELECT * FROM musictrack")
    fun getAll(): List<MusicTrack>

    @Query("SELECT * FROM musictrack WHERE type = :type")
    fun getAllByType(type: MusicTrackType): List<MusicTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: MusicTrack)

    @Delete
    fun delete(user: MusicTrack)

    @Query("SELECT EXISTS(SELECT * FROM musictrack WHERE url = :url)")
    fun isExist(url: String): Boolean

    @Query("SELECT filePath FROM musictrack WHERE url = :url")
    fun getPathByUrl(url: String): String?

    @Query("SELECT * FROM musictrack WHERE url = :url")
    fun getByUrl(url: String): MusicTrack?
}
