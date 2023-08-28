package ru.volgadev.cardrepository.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory

@Database(entities = [Card::class, CardCategory::class], version = 3)
@TypeConverters(ListStringConverter::class)
internal abstract class CardDatabaseImpl : CardDatabaseInterface, RoomDatabase() {

    abstract override fun dao(): CardDaoImpl

    companion object {
        @Volatile
        private var INSTANCE: CardDatabaseInterface? = null

        private const val DATABASE_NAME = "database.db"

        fun getInstance(context: Context): CardDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                CardDatabaseImpl::class.java, DATABASE_NAME
            ).fallbackToDestructiveMigration().build()
    }
}