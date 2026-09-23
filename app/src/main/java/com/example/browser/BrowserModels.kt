package com.example.browser

import java.util.UUID

enum class SearchEngine(val displayName: String, val searchUrlPattern: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=%s", "https://www.google.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s", "https://duckduckgo.com"),
    BRAVE("Brave", "https://search.brave.com/search?q=%s", "https://search.brave.com"),
    BING("Bing", "https://www.bing.com/search?q=%s", "https://www.bing.com"),
    WIKIPEDIA("Wikipedia", "https://en.wikipedia.org/wiki/Special:Search?search=%s", "https://www.wikipedia.org"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=%s", "https://www.ecosia.org");

    fun formatSearchUrl(query: String): String {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        return String.format(searchUrlPattern, encodedQuery)
    }
}

data class WebTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val trackersBlocked: Int = 0,
    val isHttps: Boolean = false,
    val lastAccessed: Long = System.currentTimeMillis()
)

data class ReaderArticle(
    val title: String,
    val host: String,
    val contentHtml: String,
    val readingTimeMinutes: Int = 2
)

enum class ReaderTheme {
    LIGHT,
    SEPIA,
    DARK
}

enum class ReaderFont {
    SANS_SERIF,
    SERIF,
    MONOSPACE
}

sealed class BrowserSheet {
    object None : BrowserSheet()
    object Tabs : BrowserSheet()
    object Bookmarks : BrowserSheet()
    object History : BrowserSheet()
    object Shield : BrowserSheet()
    object Settings : BrowserSheet()
    object AddQuickDial : BrowserSheet()
    object ReaderMode : BrowserSheet()
}
