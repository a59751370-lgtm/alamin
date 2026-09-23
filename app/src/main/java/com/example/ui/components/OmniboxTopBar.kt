package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChromeReaderMode
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.WebTab

@Composable
fun OmniboxTopBar(
    tab: WebTab?,
    isBookmarked: Boolean,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenShield: () -> Unit,
    onOpenReaderMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    // Synchronize input text with tab url when not editing
    LaunchedEffect(tab?.url, isFocused) {
        if (!isFocused) {
            inputText = tab?.url.orEmpty()
        }
    }

    val displayHost = remember(tab?.url) {
        val u = tab?.url.orEmpty()
        if (u.isBlank() || u.startsWith("about:")) {
            "Search or type URL"
        } else {
            try {
                val uri = android.net.Uri.parse(u)
                uri.host ?: u
            } catch (e: Exception) {
                u
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Shield / Security Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (tab?.isHttps == true) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(onClick = onOpenShield)
                        .testTag("shield_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tab?.isHttps == true) Icons.Default.Lock else Icons.Default.Security,
                        contentDescription = "Security & Shield",
                        tint = if (tab?.isHttps == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center Omnibox Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    if (inputText.isNotBlank()) {
                                        onNavigate(inputText)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { state ->
                                    isFocused = state.isFocused
                                    if (state.isFocused && inputText.startsWith("https://")) {
                                        // Keep as is for easy editing
                                    }
                                }
                                .testTag("omnibox_input"),
                            decorationBox = { innerTextField ->
                                if (inputText.isBlank() && !isFocused) {
                                    Text(
                                        text = displayHost,
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    innerTextField()
                                }
                            }
                        )

                        // Clear or Reader button inside Omnibox
                        if (isFocused && inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { inputText = "" },
                                modifier = Modifier.size(28.dp).testTag("omnibox_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear input",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (!isFocused && tab?.url?.isNotBlank() == true && !tab.url.startsWith("about:")) {
                            IconButton(
                                onClick = onOpenReaderMode,
                                modifier = Modifier.size(28.dp).testTag("reader_mode_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChromeReaderMode,
                                    contentDescription = "Reader Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Bookmark Toggle Button
                if (tab?.url?.isNotBlank() == true && !tab.url.startsWith("about:")) {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(38.dp).testTag("bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Reload or Stop Button
                IconButton(
                    onClick = {
                        if (tab?.isLoading == true) onStop() else onReload()
                    },
                    modifier = Modifier.size(38.dp).testTag("reload_button")
                ) {
                    Icon(
                        imageVector = if (tab?.isLoading == true) Icons.Default.Close else Icons.Default.Refresh,
                        contentDescription = if (tab?.isLoading == true) "Stop" else "Reload",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Animated Loading Progress Bar
            AnimatedVisibility(
                visible = tab?.isLoading == true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val p = tab?.progress ?: 0
                LinearProgressIndicator(
                    progress = { (p / 100f).coerceIn(0.05f, 1.0f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .testTag("loading_progress_bar"),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
