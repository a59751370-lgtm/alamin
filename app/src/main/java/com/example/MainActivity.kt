package com.example

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.browser.BrowserSheet
import com.example.browser.BrowserViewModel
import com.example.browser.WebTab
import com.example.ui.components.AddQuickDialDialog
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.FindInPageBar
import com.example.ui.components.HistorySheet
import com.example.ui.components.HomeSpeedDialView
import com.example.ui.components.OmniboxTopBar
import com.example.ui.components.ReaderModeView
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShieldDialog
import com.example.ui.components.TabSwitcherSheet
import com.example.ui.components.WebViewContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels {
        BrowserViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle external URLs opened with Alamin Browser
        intent?.dataString?.let { url ->
            if (url.isNotBlank()) {
                viewModel.createNewTab(url)
            }
        }

        setContent {
            val tabs by viewModel.tabs.collectAsStateWithLifecycle()
            val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
            val isIncognito = activeTab?.isIncognito == true

            MyApplicationTheme(isIncognito = isIncognito) {
                AlaminBrowserApp(
                    viewModel = viewModel,
                    tabs = tabs,
                    activeTab = activeTab
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.dataString?.let { url ->
            if (url.isNotBlank()) {
                viewModel.createNewTab(url)
            }
        }
    }
}

@Composable
fun AlaminBrowserApp(
    viewModel: BrowserViewModel,
    tabs: List<WebTab>,
    activeTab: WebTab?
) {
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val isShieldEnabled by viewModel.isShieldEnabled.collectAsStateWithLifecycle()
    val isDarkWebEnabled by viewModel.isDarkWebEnabled.collectAsStateWithLifecycle()
    val isJavaScriptEnabled by viewModel.isJavaScriptEnabled.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val totalTrackersBlocked by viewModel.totalTrackersBlocked.collectAsStateWithLifecycle()

    val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val allQuickDials by viewModel.allQuickDials.collectAsStateWithLifecycle()

    // Reader Mode & Find in page states
    val readerArticle by viewModel.readerArticle.collectAsStateWithLifecycle()
    val readerTheme by viewModel.readerTheme.collectAsStateWithLifecycle()
    val readerFont by viewModel.readerFont.collectAsStateWithLifecycle()
    val readerFontSize by viewModel.readerFontSize.collectAsStateWithLifecycle()

    val isFindInPageVisible by viewModel.isFindInPageVisible.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val findMatches by viewModel.findMatches.collectAsStateWithLifecycle()
    var findTriggerNext by remember { mutableIntStateOf(0) }
    var findTriggerPrev by remember { mutableIntStateOf(0) }

    var currentWebView by remember { mutableStateOf<WebView?>(null) }

    val isCurrentBookmarked = remember(activeTab?.url, allBookmarks) {
        val u = activeTab?.url.orEmpty()
        u.isNotBlank() && allBookmarks.any { it.url == u }
    }

    // Handle System Back Press
    BackHandler(enabled = true) {
        when {
            activeSheet != BrowserSheet.None -> {
                viewModel.closeSheet()
            }
            isFindInPageVisible -> {
                viewModel.hideFindInPage()
            }
            activeTab?.canGoBack == true -> {
                currentWebView?.goBack()
            }
            activeTab?.url?.isNotBlank() == true && !activeTab.url.startsWith("about:") -> {
                // Navigate to home speed dial
                viewModel.loadUrl("")
            }
            tabs.size > 1 -> {
                activeTab?.let { viewModel.closeTab(it.id) }
            }
            else -> {
                // Default back: minimize/exit activity
                (currentWebView?.context as? ComponentActivity)?.finish()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                OmniboxTopBar(
                    tab = activeTab,
                    isBookmarked = isCurrentBookmarked,
                    onNavigate = { input -> viewModel.loadUrl(input) },
                    onReload = { currentWebView?.reload() },
                    onStop = { currentWebView?.stopLoading() },
                    onToggleBookmark = {
                        viewModel.toggleBookmarkCurrentPage(
                            currentUrl = activeTab?.url.orEmpty(),
                            currentTitle = activeTab?.title.orEmpty()
                        )
                    },
                    onOpenShield = { viewModel.openSheet(BrowserSheet.Shield) },
                    onOpenReaderMode = {
                        if (readerArticle != null) {
                            viewModel.openSheet(BrowserSheet.ReaderMode)
                        } else {
                            // Trigger reader mode directly
                            viewModel.extractReaderMode()
                        }
                    }
                )

                AnimatedVisibility(
                    visible = isFindInPageVisible,
                    enter = slideInVertically(),
                    exit = slideOutVertically()
                ) {
                    FindInPageBar(
                        query = findQuery,
                        currentMatch = findMatches.first,
                        totalMatches = findMatches.second,
                        onQueryChange = { viewModel.setFindQuery(it) },
                        onNextMatch = { findTriggerNext++ },
                        onPrevMatch = { findTriggerPrev++ },
                        onClose = { viewModel.hideFindInPage() }
                    )
                }
            }
        },
        bottomBar = {
            BrowserBottomBar(
                tab = activeTab,
                tabsCount = tabs.size,
                isDesktopMode = activeTab?.isDesktopMode == true,
                isDarkWebEnabled = isDarkWebEnabled,
                onBack = { currentWebView?.goBack() },
                onForward = { currentWebView?.goForward() },
                onHome = { viewModel.loadUrl("") },
                onOpenTabs = { viewModel.openSheet(BrowserSheet.Tabs) },
                onOpenBookmarks = { viewModel.openSheet(BrowserSheet.Bookmarks) },
                onOpenHistory = { viewModel.openSheet(BrowserSheet.History) },
                onOpenSettings = { viewModel.openSheet(BrowserSheet.Settings) },
                onFindInPage = { viewModel.showFindInPage() },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onToggleDarkWeb = { viewModel.toggleDarkWeb() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val url = activeTab?.url.orEmpty()
            if (url.isBlank() || url.startsWith("about:")) {
                HomeSpeedDialView(
                    searchEngine = searchEngine,
                    quickDials = allQuickDials,
                    recentHistory = recentHistory,
                    totalTrackersBlocked = totalTrackersBlocked,
                    isShieldEnabled = isShieldEnabled,
                    isIncognito = activeTab?.isIncognito == true,
                    onNavigate = { viewModel.loadUrl(it) },
                    onOpenShield = { viewModel.openSheet(BrowserSheet.Shield) },
                    onOpenSearchEnginePicker = { viewModel.openSheet(BrowserSheet.Settings) },
                    onAddQuickDialClicked = { viewModel.openSheet(BrowserSheet.AddQuickDial) },
                    onDeleteQuickDial = { viewModel.deleteQuickDial(it) }
                )
            } else if (activeTab != null) {
                WebViewContainer(
                    tab = activeTab,
                    isShieldEnabled = isShieldEnabled,
                    isDarkWebEnabled = isDarkWebEnabled,
                    isJavaScriptEnabled = isJavaScriptEnabled,
                    findQuery = findQuery,
                    findTriggerNext = findTriggerNext,
                    findTriggerPrev = findTriggerPrev,
                    onTabUpdated = { pageUrl, title, canBack, canFwd ->
                        viewModel.updateTabUrlAndTitle(activeTab.id, pageUrl, title, canBack, canFwd)
                    },
                    onLoadingChanged = { isLoading, progress ->
                        viewModel.updateTabLoading(activeTab.id, isLoading, progress)
                    },
                    onTrackerBlocked = {
                        viewModel.incrementBlockedTrackers()
                    },
                    onFindMatchesUpdated = { cur, total ->
                        viewModel.setFindMatches(cur, total)
                    },
                    onReaderArticleExtracted = { article ->
                        viewModel.setReaderArticle(article)
                    },
                    onWebViewCreated = { webViewInstance ->
                        currentWebView = webViewInstance
                    }
                )
            }
        }
    }

    // Modal Sheets & Dialogs
    when (activeSheet) {
        BrowserSheet.Tabs -> {
            TabSwitcherSheet(
                tabs = tabs,
                activeTabId = activeTab?.id.orEmpty(),
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { isIncognito -> viewModel.createNewTab(isIncognito = isIncognito) },
                onCloseAllTabs = { incognitoOnly -> viewModel.closeAllTabs(incognitoOnly) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        BrowserSheet.Bookmarks -> {
            BookmarksSheet(
                bookmarks = allBookmarks,
                onSelectBookmark = {
                    viewModel.loadUrl(it)
                    viewModel.closeSheet()
                },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        BrowserSheet.History -> {
            HistorySheet(
                history = allHistory,
                onSelectHistoryItem = {
                    viewModel.loadUrl(it)
                    viewModel.closeSheet()
                },
                onDeleteHistoryItem = { viewModel.deleteHistory(it) },
                onClearAllHistory = { viewModel.clearAllHistory() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        BrowserSheet.Shield -> {
            ShieldDialog(
                isShieldEnabled = isShieldEnabled,
                isDesktopMode = activeTab?.isDesktopMode == true,
                isDarkWebEnabled = isDarkWebEnabled,
                isJavaScriptEnabled = isJavaScriptEnabled,
                isHttps = activeTab?.isHttps == true,
                pageBlockedCount = activeTab?.trackersBlocked ?: 0,
                totalBlockedCount = totalTrackersBlocked,
                onToggleShield = { viewModel.toggleShield() },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onToggleDarkWeb = { viewModel.toggleDarkWeb() },
                onToggleJavaScript = { viewModel.toggleJavaScript() },
                onClearCacheAndCookies = {
                    viewModel.clearBrowsingData(clearCache = true, clearHistory = false, clearCookies = true)
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        BrowserSheet.Settings -> {
            SettingsDialog(
                selectedEngine = searchEngine,
                onSelectSearchEngine = { viewModel.setSearchEngine(it) },
                onClearBrowsingData = {
                    viewModel.clearBrowsingData(clearCache = true, clearHistory = true, clearCookies = true)
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        BrowserSheet.AddQuickDial -> {
            AddQuickDialDialog(
                onAdd = { title, targetUrl ->
                    viewModel.addQuickDial(title, targetUrl)
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        BrowserSheet.ReaderMode -> {
            readerArticle?.let { article ->
                ReaderModeView(
                    article = article,
                    theme = readerTheme,
                    font = readerFont,
                    fontSize = readerFontSize,
                    onSetTheme = { viewModel.setReaderTheme(it) },
                    onSetFont = { viewModel.setReaderFont(it) },
                    onAdjustFontSize = { viewModel.adjustReaderFontSize(it) },
                    onClose = { viewModel.closeSheet() }
                )
            }
        }
        BrowserSheet.None -> {}
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

