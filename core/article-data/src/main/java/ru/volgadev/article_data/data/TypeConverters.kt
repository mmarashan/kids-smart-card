package ru.volgadev.article_data.storage

import androidx.room.TypeConverter
import ru.volgadev.article_data.domain.ArticleType
import java.util.stream.Collectors

internal class ListStringConverter {

    private val DELIMITER = ","

    @TypeConverter
    fun from(hobbies: List<String>): String {
        return hobbies.stream().collect(Collectors.joining(DELIMITER))
    }

    @TypeConverter
    fun to(data: String): List<String> {
        return if (data.isEmpty()) listOf() else data.split(DELIMITER)
    }
}

internal class ArticleTypeConverter {

    @TypeConverter
    fun from(type: ArticleType): String {
        return type.name
    }

    @TypeConverter
    fun to(data: String): ArticleType {
        return ArticleType.valueOf(data)
    }
}