package com.example.browser

import android.app.Application
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BrowserRepository
import com.example.data.model.BookmarkEntity
import com.example.data.model.HistoryEntity
import com.example.data.model.QuickDialEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URI

class BrowserViewModel(
    application: Application,
    private val repository: BrowserRepository
) : AndroidViewModel(application) {

    private val _tabs = MutableStateFlow<List<WebTab>>(listOf(WebTab()))
    val tabs: StateFlow<List<WebTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    private val _searchEngine = MutableStateFlow(SearchEngine.GOOGLE)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _isShieldEnabled = MutableStateFlow(true)
    val isShieldEnabled: StateFlow<Boolean> = _isShieldEnabled.asStateFlow()

    private val _isDarkWebEnabled = MutableStateFlow(false)
    val isDarkWebEnabled: StateFlow<Boolean> = _isDarkWebEnabled.asStateFlow()

    private val _isJavaScriptEnabled = MutableStateFlow(true)
    val isJavaScriptEnabled: StateFlow<Boolean> = _isJavaScriptEnabled.asStateFlow()

    private val _activeSheet = MutableStateFlow<BrowserSheet>(BrowserSheet.None)
    val activeSheet: StateFlow<BrowserSheet> = _activeSheet.asStateFlow()

    private val _totalTrackersBlocked = MutableStateFlow(42)
    val totalTrackersBlocked: StateFlow<Int> = _totalTrackersBlocked.asStateFlow()

    // Reader Mode State
    private val _readerArticle = MutableStateFlow<ReaderArticle?>(null)
    val readerArticle: StateFlow<ReaderArticle?> = _readerArticle.asStateFlow()

    private val _readerTheme = MutableStateFlow(ReaderTheme.LIGHT)
    val readerTheme: StateFlow<ReaderTheme> = _readerTheme.asStateFlow()

    private val _readerFont = MutableStateFlow(ReaderFont.SERIF)
    val readerFont: StateFlow<ReaderFont> = _readerFont.asStateFlow()

    private val _readerFontSize = MutableStateFlow(18f)
    val readerFontSize: StateFlow<Float> = _readerFontSize.asStateFlow()

    // Find in Page State
    private val _isFindInPageVisible = MutableStateFlow(false)
    val isFindInPageVisible: StateFlow<Boolean> = _isFindInPageVisible.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _findMatches = MutableStateFlow(Pair(0, 0)) // current, total
    val findMatches: StateFlow<Pair<Int, Int>> = _findMatches.asStateFlow()

    // Database flows
    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistory: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<HistoryEntity>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuickDials: StateFlow<List<QuickDialEntity>> = repository.allQuickDials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTab: StateFlow<WebTab?> = combine(_tabs, _activeTabId) { tabs, activeId ->
        tabs.find { it.id == activeId } ?: tabs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _tabs.value.firstOrNull())

    fun createNewTab(url: String = "", isIncognito: Boolean = false) {
        val newTab = WebTab(
            url = url,
            isIncognito = isIncognito,
            title = if (url.isBlank()) (if (isIncognito) "Incognito Tab" else "New Tab") else url,
            isHttps = url.startsWith("https://")
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        _activeSheet.value = BrowserSheet.None
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) {
            // Keep at least one tab open
            val freshTab = WebTab(isIncognito = currentTabs.first().isIncognito)
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
            return
        }
        val remaining = currentTabs.filter { it.id != tabId }
        _tabs.value = remaining
        if (_activeTabId.value == tabId) {
            _activeTabId.value = remaining.last().id
        }
    }

    fun closeAllTabs(incognitoOnly: Boolean = false) {
        if (incognitoOnly) {
            val normalTabs = _tabs.value.filter { !it.isIncognito }
            if (normalTabs.isEmpty()) {
                val newTab = WebTab()
                _tabs.value = listOf(newTab)
                _activeTabId.value = newTab.id
            } else {
                _tabs.value = normalTabs
                _activeTabId.value = normalTabs.first().id
            }
        } else {
            val freshTab = WebTab()
            _tabs.value = listOf(freshTab)
            _activeTabId.value = freshTab.id
        }
        _activeSheet.value = BrowserSheet.None
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
        _activeSheet.value = BrowserSheet.None
    }

    fun openSheet(sheet: BrowserSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = BrowserSheet.None
    }

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
    }

    fun toggleShield() {
        _isShieldEnabled.value = !_isShieldEnabled.value
    }

    fun toggleDarkWeb() {
        _isDarkWebEnabled.value = !_isDarkWebEnabled.value
    }

    fun toggleJavaScript() {
        _isJavaScriptEnabled.value = !_isJavaScriptEnabled.value
    }

    fun toggleDesktopMode() {
        val tabId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(isDesktopMode = !tab.isDesktopMode)
            } else tab
        }
    }

    fun incrementBlockedTrackers() {
        _totalTrackersBlocked.value += 1
        val tabId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(trackersBlocked = tab.trackersBlocked + 1)
            } else tab
        }
    }

    fun updateTabLoading(tabId: String, isLoading: Boolean, progress: Int) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(isLoading = isLoading, progress = progress)
            } else tab
        }
    }

    fun updateTabUrlAndTitle(tabId: String, url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        val resolvedTitle = if (!title.isNullOrBlank()) title else url
        val isHttps = url.startsWith("https://")
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(
                    url = url,
                    title = resolvedTitle,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    isHttps = isHttps
                )
            } else tab
        }

        val currentTab = _tabs.value.find { it.id == tabId }
        if (currentTab != null && !currentTab.isIncognito && url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("data:")) {
            viewModelScope.launch {
                repository.recordHistory(resolvedTitle, url)
            }
        }
    }

    fun resolveInputToUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""

        // Check if it's already a URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") || trimmed.startsWith("about:")) {
            return trimmed
        }

        // Check if it looks like a domain name (e.g., example.com, google.co.uk, 192.168.1.1, localhost:8080)
        val hasWhitespace = trimmed.any { it.isWhitespace() }
        val looksLikeDomain = !hasWhitespace && (
            trimmed.contains(".") && !trimmed.endsWith(".") ||
            trimmed.startsWith("localhost") ||
            trimmed.contains(":")
        )

        return if (looksLikeDomain) {
            "https://$trimmed"
        } else {
            _searchEngine.value.formatSearchUrl(trimmed)
        }
    }

    fun loadUrl(input: String) {
        val resolved = resolveInputToUrl(input)
        val tabId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(url = resolved, title = "Loading...", trackersBlocked = 0, isHttps = resolved.startsWith("https://"))
            } else tab
        }
    }

    fun toggleBookmarkCurrentPage(currentUrl: String, currentTitle: String) {
        viewModelScope.launch {
            if (currentUrl.isBlank() || currentUrl.startsWith("about:")) return@launch
            val existing = allBookmarks.value.find { it.url == currentUrl }
            if (existing != null) {
                repository.deleteBookmark(existing)
            } else {
                repository.addBookmark(currentTitle, currentUrl)
            }
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    fun deleteHistory(history: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(history)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun addQuickDial(title: String, url: String) {
        viewModelScope.launch {
            repository.addQuickDial(title, url)
        }
    }

    fun deleteQuickDial(quickDial: QuickDialEntity) {
        viewModelScope.launch {
            repository.deleteQuickDial(quickDial)
        }
    }

    fun clearBrowsingData(clearCache: Boolean, clearHistory: Boolean, clearCookies: Boolean) {
        viewModelScope.launch {
            if (clearHistory) {
                repository.clearHistory()
            }
            if (clearCookies) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            if (clearCache) {
                WebStorage.getInstance().deleteAllData()
            }
        }
    }

    // Reader Mode controls
    fun setReaderArticle(article: ReaderArticle?) {
        _readerArticle.value = article
        if (article != null) {
            _activeSheet.value = BrowserSheet.ReaderMode
        }
    }

    fun extractReaderMode() {
        val currentTab = activeTab.value ?: return
        if (_readerArticle.value == null) {
            val hostStr = try { URI(currentTab.url).host ?: currentTab.url } catch (e: Exception) { currentTab.url }
            _readerArticle.value = ReaderArticle(
                title = currentTab.title,
                host = hostStr,
                contentHtml = "<p>Formatting web content from $hostStr into reader mode for clean, distraction-free reading.</p>",
                readingTimeMinutes = 2
            )
        }
        _activeSheet.value = BrowserSheet.ReaderMode
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerTheme.value = theme
    }

    fun setReaderFont(font: ReaderFont) {
        _readerFont.value = font
    }

    fun adjustReaderFontSize(delta: Float) {
        val updated = (_readerFontSize.value + delta).coerceIn(12f, 32f)
        _readerFontSize.value = updated
    }

    // Find In Page controls
    fun showFindInPage() {
        _isFindInPageVisible.value = true
        _findMatches.value = Pair(0, 0)
    }

    fun hideFindInPage() {
        _isFindInPageVisible.value = false
        _findQuery.value = ""
        _findMatches.value = Pair(0, 0)
    }

    fun setFindQuery(query: String) {
        _findQuery.value = query
    }

    fun setFindMatches(current: Int, total: Int) {
        _findMatches.value = Pair(current, total)
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = BrowserRepository(db.bookmarkDao(), db.historyDao(), db.quickDialDao())
                    return BrowserViewModel(application, repo) as T
                }
            }
    }
}
