package ru.volgadev.cardrepository.data.database

import androidx.room.TypeConverter
import java.util.stream.Collectors

internal class ListStringConverter {

    private val DELIMITER = ","

    @TypeConverter
    fun from(items: List<String>): String {
        return items.stream().collect(Collectors.joining(DELIMITER))
    }

    @TypeConverter
    fun to(data: String): List<String> {
        return if (data.isEmpty()) listOf() else data.split(DELIMITER)
    }
}