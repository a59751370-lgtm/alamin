package com.example.data

import com.example.data.dao.BookmarkDao
import com.example.data.dao.HistoryDao
import com.example.data.dao.QuickDialDao
import com.example.data.model.BookmarkEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.QuickDialEntity
import kotlinx.coroutines.flow.Flow

class BrowserRepository(
    private val bookmarkDao: BookmarkDao,
    private val historyDao: HistoryDao,
    private val quickDialDao: QuickDialDao
) {
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val recentHistory: Flow<List<HistoryEntity>> = historyDao.getRecentHistory()
    val allQuickDials: Flow<List<QuickDialEntity>> = quickDialDao.getAllQuickDials()

    fun isBookmarked(url: String): Flow<Boolean> = bookmarkDao.isBookmarked(url)

    suspend fun addBookmark(title: String, url: String) {
        if (url.isBlank()) return
        bookmarkDao.insertBookmark(BookmarkEntity(title = title.ifBlank { url }, url = url))
    }

    suspend fun removeBookmarkByUrl(url: String) {
        bookmarkDao.deleteBookmarkByUrl(url)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun recordHistory(title: String, url: String) {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("data:")) return
        historyDao.insertHistory(HistoryEntity(title = title.ifBlank { url }, url = url))
    }

    suspend fun deleteHistory(history: HistoryEntity) {
        historyDao.deleteHistory(history)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun addQuickDial(title: String, url: String) {
        val currentCount = quickDialDao.getCount()
        quickDialDao.insertQuickDial(
            QuickDialEntity(
                title = title.ifBlank { url },
                url = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url,
                iconKey = "custom",
                orderIndex = currentCount
            )
        )
    }

    suspend fun deleteQuickDial(quickDial: QuickDialEntity) {
        quickDialDao.deleteQuickDial(quickDial)
    }

    fun searchBookmarks(query: String): Flow<List<BookmarkEntity>> = bookmarkDao.searchBookmarks(query)
    fun searchHistory(query: String): Flow<List<HistoryEntity>> = historyDao.searchHistory(query)
}
