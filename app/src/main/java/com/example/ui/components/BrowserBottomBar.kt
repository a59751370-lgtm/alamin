package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.WebTab

@Composable
fun BrowserBottomBar(
    tab: WebTab?,
    tabsCount: Int,
    isDesktopMode: Boolean,
    isDarkWebEnabled: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleDarkWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                enabled = tab?.canGoBack == true,
                modifier = Modifier.size(48.dp).testTag("bottom_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back",
                    tint = if (tab?.canGoBack == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }

            // Forward Button
            IconButton(
                onClick = onForward,
                enabled = tab?.canGoForward == true,
                modifier = Modifier.size(48.dp).testTag("bottom_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go Forward",
                    tint = if (tab?.canGoForward == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }

            // Home Button
            IconButton(
                onClick = onHome,
                modifier = Modifier.size(48.dp).testTag("bottom_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tab Switcher Button (with counter badge)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onOpenTabs)
                    .testTag("bottom_tabs_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabsCount.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // More Options Menu Button
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(48.dp).testTag("bottom_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.testTag("browser_more_menu")
                ) {
                    DropdownMenuItem(
                        text = { Text("Bookmarks") },
                        leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenBookmarks()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("History") },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenHistory()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Find in page") },
                        leadingIcon = { Icon(Icons.Default.FindInPage, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onFindInPage()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share link") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            val u = tab?.url.orEmpty()
                            if (u.isNotBlank() && !u.startsWith("about:")) {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, u)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share webpage"))
                            }
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Desktop site") },
                        leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null) },
                        trailingIcon = {
                            Switch(
                                checked = isDesktopMode,
                                onCheckedChange = {
                                    showMenu = false
                                    onToggleDesktopMode()
                                }
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleDesktopMode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Force dark web") },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                        trailingIcon = {
                            Switch(
                                checked = isDarkWebEnabled,
                                onCheckedChange = {
                                    showMenu = false
                                    onToggleDarkWeb()
                                }
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleDarkWeb()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    }
}
