package ru.volgadev.article_repository.data.database

import androidx.room.TypeConverter
import ru.volgadev.article_repository.domain.model.ArticleType
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

internal class ArticleTypeConverter {

    @TypeConverter
    fun from(item: ArticleType): String {
        return item.name
    }

    @TypeConverter
    fun to(data: String): ArticleType {
        return ArticleType.valueOf(data)
    }
}