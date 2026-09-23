package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.browser.AdBlockFilter
import com.example.browser.ReaderArticle
import com.example.browser.WebTab
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    tab: WebTab,
    isShieldEnabled: Boolean,
    isDarkWebEnabled: Boolean,
    isJavaScriptEnabled: Boolean,
    findQuery: String,
    findTriggerNext: Int,
    findTriggerPrev: Int,
    onTabUpdated: (url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onLoadingChanged: (isLoading: Boolean, progress: Int) -> Unit,
    onTrackerBlocked: () -> Unit,
    onFindMatchesUpdated: (current: Int, total: Int) -> Unit,
    onReaderArticleExtracted: (ReaderArticle) -> Unit,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    val context = LocalContext.current
    val webView = remember(tab.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    DisposableEffect(tab.id) {
        onWebViewCreated(webView)
        onDispose {
            webView.stopLoading()
        }
    }

    // Configure WebSettings
    LaunchedEffect(tab.isDesktopMode, isJavaScriptEnabled, isDarkWebEnabled) {
        webView.settings.apply {
            javaScriptEnabled = isJavaScriptEnabled
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true

            // Desktop user agent toggle
            if (tab.isDesktopMode) {
                userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            } else {
                userAgentString = null // Default mobile user agent
            }

            // Force dark web styling if enabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    @Suppress("DEPRECATION")
                    if (isDarkWebEnabled) {
                        forceDark = WebSettings.FORCE_DARK_ON
                    } else {
                        forceDark = WebSettings.FORCE_DARK_OFF
                    }
                } catch (ignored: Exception) {}
            }
        }
    }

    // Set WebViewClient and WebChromeClient
    LaunchedEffect(isShieldEnabled) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (isShieldEnabled && AdBlockFilter.isAdOrTracker(request?.url)) {
                    view?.post { onTrackerBlocked() }
                    return AdBlockFilter.createEmptyResponse()
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onLoadingChanged(true, 15)
                onTabUpdated(
                    url.orEmpty(),
                    view?.title,
                    view?.canGoBack() == true,
                    view?.canGoForward() == true
                )
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onLoadingChanged(false, 100)
                onTabUpdated(
                    url.orEmpty(),
                    view?.title,
                    view?.canGoBack() == true,
                    view?.canGoForward() == true
                )

                // Inject Reader Mode extraction script
                val extractScript = """
                    (function() {
                        try {
                            var title = document.title || '';
                            var h1 = document.querySelector('h1');
                            if (h1 && h1.innerText) {
                                title = h1.innerText;
                            }
                            var article = document.querySelector('article') || document.querySelector('main') || document.body;
                            var paragraphs = article.querySelectorAll('p, h2, h3, blockquote');
                            var contentParts = [];
                            for (var i = 0; i < paragraphs.length; i++) {
                                var text = paragraphs[i].innerText.trim();
                                if (text.length > 25) {
                                    contentParts.push('<p>' + text + '</p>');
                                }
                            }
                            return JSON.stringify({
                                title: title,
                                contentHtml: contentParts.join(''),
                                wordCount: contentParts.join(' ').split(' ').length
                            });
                        } catch(e) {
                            return null;
                        }
                    })();
                """.trimIndent()

                view?.evaluateJavascript(extractScript) { resultJson ->
                    try {
                        if (!resultJson.isNullOrBlank() && resultJson != "null") {
                            // evaluateJavascript wraps return in json quotes, unwrap if needed
                            val jsonString = if (resultJson.startsWith("\"") && resultJson.endsWith("\"")) {
                                org.json.JSONTokener(resultJson).nextValue().toString()
                            } else {
                                resultJson
                            }
                            val json = JSONObject(jsonString)
                            val title = json.optString("title", view.title ?: "Article")
                            val content = json.optString("contentHtml", "")
                            val wordCount = json.optInt("wordCount", 0)
                            if (content.isNotBlank()) {
                                val host = Uri.parse(url ?: "").host ?: ""
                                val minutes = (wordCount / 180).coerceAtLeast(1)
                                onReaderArticleExtracted(
                                    ReaderArticle(
                                        title = title,
                                        host = host,
                                        contentHtml = content,
                                        readingTimeMinutes = minutes
                                    )
                                )
                            }
                        }
                    } catch (ignored: Exception) {}
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase() ?: return false
                if (scheme != "http" && scheme != "https") {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        return true
                    }
                }
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onLoadingChanged(newProgress < 100, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onTabUpdated(
                    view?.url.orEmpty(),
                    title,
                    view?.canGoBack() == true,
                    view?.canGoForward() == true
                )
            }
        }
    }

    // Handle URL loading
    LaunchedEffect(tab.url) {
        if (tab.url.isNotBlank() && !tab.url.startsWith("about:") && webView.url != tab.url) {
            webView.loadUrl(tab.url)
        }
    }

    // Find in Page handling
    LaunchedEffect(findQuery) {
        if (findQuery.isNotBlank()) {
            webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                onFindMatchesUpdated(activeMatchOrdinal + 1, numberOfMatches)
            }
            webView.findAllAsync(findQuery)
        } else {
            webView.clearMatches()
            onFindMatchesUpdated(0, 0)
        }
    }

    LaunchedEffect(findTriggerNext) {
        if (findTriggerNext > 0) {
            webView.findNext(true)
        }
    }

    LaunchedEffect(findTriggerPrev) {
        if (findTriggerPrev > 0) {
            webView.findNext(false)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
