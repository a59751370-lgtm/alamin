package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BookmarkDao
import com.example.data.dao.HistoryDao
import com.example.data.dao.QuickDialDao
import com.example.data.model.BookmarkEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.QuickDialEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        QuickDialEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun quickDialDao(): QuickDialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brawjar_browser.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultQuickDials = listOf(
                                QuickDialEntity(title = "Google", url = "https://www.google.com", iconKey = "google", isDefault = true, orderIndex = 0),
                                QuickDialEntity(title = "Wikipedia", url = "https://www.wikipedia.org", iconKey = "wikipedia", isDefault = true, orderIndex = 1),
                                QuickDialEntity(title = "YouTube", url = "https://www.youtube.com", iconKey = "youtube", isDefault = true, orderIndex = 2),
                                QuickDialEntity(title = "GitHub", url = "https://github.com", iconKey = "github", isDefault = true, orderIndex = 3),
                                QuickDialEntity(title = "Reddit", url = "https://www.reddit.com", iconKey = "reddit", isDefault = true, orderIndex = 4),
                                QuickDialEntity(title = "DuckDuckGo", url = "https://duckduckgo.com", iconKey = "duckduckgo", isDefault = true, orderIndex = 5),
                                QuickDialEntity(title = "BBC News", url = "https://www.bbc.com/news", iconKey = "news", isDefault = true, orderIndex = 6),
                                QuickDialEntity(title = "Hacker News", url = "https://news.ycombinator.com", iconKey = "hackernews", isDefault = true, orderIndex = 7)
                            )
                            INSTANCE?.quickDialDao()?.insertAll(defaultQuickDials)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
