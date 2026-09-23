package com.example.browser

import android.net.Uri
import java.io.ByteArrayInputStream
import android.webkit.WebResourceResponse

object AdBlockFilter {
    private val blockedHosts = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adnxs.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "adsystem.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "advertising.com",
        "adcolony.com",
        "applovin.com",
        "chartbeat.com",
        "scorecardresearch.com",
        "quantserve.com",
        "hotjar.com",
        "popads.net",
        "popcash.net",
        "adroll.com",
        "moatads.com",
        "casalemedia.com",
        "smartadserver.com",
        "amazon-adsystem.com",
        "bidswitch.net",
        "serving-sys.com",
        "zemanta.com"
    )

    fun isAdOrTracker(uri: Uri?): Boolean {
        if (uri == null) return false
        val host = uri.host?.lowercase() ?: return false
        for (blocked in blockedHosts) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return true
            }
        }
        val path = uri.path?.lowercase() ?: ""
        if (path.contains("/adserver") || path.contains("/ads.js") || path.contains("/track.js") || path.contains("/telemetry")) {
            return true
        }
        return false
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
