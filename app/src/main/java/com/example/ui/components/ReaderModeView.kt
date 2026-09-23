package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.ReaderArticle
import com.example.browser.ReaderFont
import com.example.browser.ReaderTheme
import com.example.ui.theme.SepiaBg
import com.example.ui.theme.SepiaSurface
import com.example.ui.theme.SepiaText

@Composable
fun ReaderModeView(
    article: ReaderArticle,
    theme: ReaderTheme,
    font: ReaderFont,
    fontSize: Float,
    onSetTheme: (ReaderTheme) -> Unit,
    onSetFont: (ReaderFont) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (theme) {
        ReaderTheme.LIGHT -> Color(0xFFFAFBFD)
        ReaderTheme.SEPIA -> SepiaBg
        ReaderTheme.DARK -> Color(0xFF121212)
    }

    val textColor = when (theme) {
        ReaderTheme.LIGHT -> Color(0xFF1E293B)
        ReaderTheme.SEPIA -> SepiaText
        ReaderTheme.DARK -> Color(0xFFE2E8F0)
    }

    val surfaceColor = when (theme) {
        ReaderTheme.LIGHT -> Color(0xFFF1F5F9)
        ReaderTheme.SEPIA -> SepiaSurface
        ReaderTheme.DARK -> Color(0xFF1E1E1E)
    }

    val fontFamily = when (font) {
        ReaderFont.SERIF -> FontFamily.Serif
        ReaderFont.SANS_SERIF -> FontFamily.SansSerif
        ReaderFont.MONOSPACE -> FontFamily.Monospace
    }

    // Strip HTML paragraph tags for clean Compose text display
    val paragraphs = rememberFormattedParagraphs(article.contentHtml)

    Surface(
        color = backgroundColor,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("reader_mode_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Reader Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Reader Mode",
                        tint = textColor
                    )
                }

                // Font Family switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Serif",
                        fontSize = 12.sp,
                        fontWeight = if (font == ReaderFont.SERIF) FontWeight.Bold else FontWeight.Normal,
                        color = if (font == ReaderFont.SERIF) MaterialTheme.colorScheme.primary else textColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSetFont(ReaderFont.SERIF) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Sans",
                        fontSize = 12.sp,
                        fontWeight = if (font == ReaderFont.SANS_SERIF) FontWeight.Bold else FontWeight.Normal,
                        color = if (font == ReaderFont.SANS_SERIF) MaterialTheme.colorScheme.primary else textColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSetFont(ReaderFont.SANS_SERIF) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Mono",
                        fontSize = 12.sp,
                        fontWeight = if (font == ReaderFont.MONOSPACE) FontWeight.Bold else FontWeight.Normal,
                        color = if (font == ReaderFont.MONOSPACE) MaterialTheme.colorScheme.primary else textColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSetFont(ReaderFont.MONOSPACE) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                // Font Size controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onAdjustFontSize(-2f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                    IconButton(
                        onClick = { onAdjustFontSize(+2f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                }

                // Theme color circles
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFAFBFD))
                            .clickable { onSetTheme(ReaderTheme.LIGHT) }
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SepiaBg)
                            .clickable { onSetTheme(ReaderTheme.SEPIA) }
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF121212))
                            .clickable { onSetTheme(ReaderTheme.DARK) }
                    )
                }
            }

            // Article Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Host / Source
                item {
                    Text(
                        text = article.host.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Article Title
                item {
                    Text(
                        text = article.title,
                        fontSize = (fontSize + 8f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        lineHeight = (fontSize + 14f).sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Read time metadata
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${article.readingTimeMinutes} min read • Reader Mode",
                            fontSize = 13.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                    HorizontalDivider(color = textColor.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Paragraphs
                items(paragraphs.size) { index ->
                    Text(
                        text = paragraphs[index],
                        fontSize = fontSize.sp,
                        fontFamily = fontFamily,
                        lineHeight = (fontSize * 1.55f).sp,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedParagraphs(html: String): List<String> {
    return androidx.compose.runtime.remember(html) {
        if (html.isBlank()) {
            listOf("No readable article text found on this page.")
        } else {
            val unescaped = android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT).toString()
            unescaped.split("\n\n")
                .map { it.trim() }
                .filter { it.length > 20 }
                .ifEmpty { listOf(unescaped.trim().ifBlank { "Article content" }) }
        }
    }
}
