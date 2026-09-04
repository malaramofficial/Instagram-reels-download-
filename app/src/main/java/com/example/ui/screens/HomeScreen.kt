package com.example.ui.screens

import android.app.Application
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.DownloadItem
import com.example.ui.theme.InstaMagenta
import com.example.ui.theme.InstaOrange
import com.example.ui.theme.InstaPink
import com.example.ui.theme.InstaPurple
import com.example.viewmodel.DownloadViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenTab {
    DOWNLOAD, BROWSER, HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DownloadViewModel,
    initialSharedUrl: String = "",
    onSharedUrlProcessed: () -> Unit = {},
    onCloseApp: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(ScreenTab.DOWNLOAD) }
    var inputUrl by remember { mutableStateOf("") }
    
    // URL automation and share-processing flags
    var pendingUrlToAutomate by remember { mutableStateOf("") }
    var activeSharedUrl by remember { mutableStateOf("") }
    var isShareOverlayVisible by remember { mutableStateOf(false) }
    
    // Clipboard url detection
    var detectedClipboardUrl by remember { mutableStateOf("") }

    // Active video preview inside dialog
    var activeVideoUrlForPlayer by remember { mutableStateOf<String?>(null) }
    
    // Delete Confirmation
    var itemToDelete by remember { mutableStateOf<DownloadItem?>(null) }

    // Setup persistent WebView
    val webView = remember {
        WebView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            
            // Enable Cookies
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        view?.loadUrl(url)
                    }
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    // Automate filling URL and clicking Download on fastvideosave.net
                    if (pendingUrlToAutomate.isNotEmpty() && url?.contains("fastvideosave.net") == true) {
                        val js = """
                            (function() {
                                var input = document.querySelector('input[type="text"]') || 
                                            document.querySelector('input[type="url"]') || 
                                            document.querySelector('input[id*="url"]') || 
                                            document.querySelector('input');
                                if (input) {
                                    input.value = '$pendingUrlToAutomate';
                                    input.dispatchEvent(new Event('input', { bubbles: true }));
                                    input.dispatchEvent(new Event('change', { bubbles: true }));
                                    
                                    // Submit button automation
                                    setTimeout(function() {
                                        var button = document.querySelector('button[type="submit"]') || 
                                                     document.querySelector('form button') || 
                                                     document.querySelector('.btn') || 
                                                     document.querySelector('button');
                                        if (button) {
                                            button.click();
                                        }
                                    }, 500);
                                }
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(js, null)
                        pendingUrlToAutomate = "" // Clear after automation triggers
                    }
                }
            }

            // Custom Download Interceptor
            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                viewModel.enqueueDownload(url, userAgent, contentDisposition, mimetype, contentLength)
                // If sharing in background, immediately exit app back to Instagram
                if (isShareOverlayVisible) {
                    isShareOverlayVisible = false
                    onCloseApp()
                }
            }
            
            loadUrl("https://fastvideosave.net/")
        }
    }

    // Reactive listener for incoming shared URLs (cold start & new intents)
    LaunchedEffect(initialSharedUrl) {
        if (initialSharedUrl.isNotEmpty()) {
            activeSharedUrl = initialSharedUrl
            isShareOverlayVisible = true
            pendingUrlToAutomate = initialSharedUrl
            webView.loadUrl("https://fastvideosave.net/")
            onSharedUrlProcessed() // Reset intent parameter in MainActivity
        }
    }

    // Monitor clipboard on start or return to screen
    LaunchedEffect(currentTab) {
        if (currentTab == ScreenTab.DOWNLOAD) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboard.hasPrimaryClip()) {
                    val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                    if (clipText.contains("instagram.com/reel/") || clipText.contains("instagram.com/p/")) {
                        detectedClipboardUrl = clipText
                    } else {
                        detectedClipboardUrl = ""
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Reels Downloader",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InstaMagenta
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DOWNLOAD,
                    onClick = { currentTab = ScreenTab.DOWNLOAD },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Download Tab") },
                    label = { Text("Download") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = InstaPink,
                        selectedTextColor = InstaPink,
                        indicatorColor = InstaPink.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("tab_download")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.BROWSER,
                    onClick = { currentTab = ScreenTab.BROWSER },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Browser Tab") },
                    label = { Text("FastVideoSave") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = InstaPurple,
                        selectedTextColor = InstaPurple,
                        indicatorColor = InstaPurple.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("tab_browser")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HISTORY,
                    onClick = { currentTab = ScreenTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "History Tab") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = InstaOrange,
                        selectedTextColor = InstaOrange,
                        indicatorColor = InstaOrange.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.DOWNLOAD -> DownloadDashboard(
                    inputUrl = inputUrl,
                    onUrlChange = { inputUrl = it },
                    detectedClipboardUrl = detectedClipboardUrl,
                    onUseClipboard = {
                        inputUrl = detectedClipboardUrl
                        detectedClipboardUrl = ""
                    },
                    onDownloadTriggered = { url ->
                        pendingUrlToAutomate = url
                        webView.loadUrl("https://fastvideosave.net/")
                        currentTab = ScreenTab.BROWSER
                    }
                )

                ScreenTab.BROWSER -> BrowserScreen(
                    webView = webView,
                    viewModel = viewModel
                )

                ScreenTab.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onPlayVideo = { videoPath ->
                        activeVideoUrlForPlayer = videoPath
                    },
                    onDeleteRequested = { item ->
                        itemToDelete = item
                    }
                )
            }

            // Quick Background-Share Auto Processing Overlay
            if (isShareOverlayVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .clickable(enabled = false) {}, // Intercept touch events
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Vibrant Instagram gradient colored spinner
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(72.dp),
                                    color = InstaPink,
                                    strokeWidth = 6.dp
                                )
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = InstaMagenta,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "Instagram Reel Shared!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Analyzing & generating background download file link via FastVideoSave engine...",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = activeSharedUrl,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = InstaPurple,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(InstaPurple.copy(alpha = 0.05f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isShareOverlayVisible = false
                                        onCloseApp()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        isShareOverlayVisible = false
                                        currentTab = ScreenTab.BROWSER
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("Show Browser", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Video Player Dialog overlay
            activeVideoUrlForPlayer?.let { videoPath ->
                VideoPlayerDialog(
                    videoUriStr = videoPath,
                    onDismiss = { activeVideoUrlForPlayer = null }
                )
            }

            // Delete Confirm Alert Dialog
            itemToDelete?.let { item ->
                AlertDialog(
                    onDismissRequest = { itemToDelete = null },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text("Delete Download?") },
                    text = { Text("Are you sure you want to delete '${item.title}'? This will also delete the video file from your device.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteDownload(item)
                                itemToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { itemToDelete = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DownloadDashboard(
    inputUrl: String,
    onUrlChange: (String) -> Unit,
    detectedClipboardUrl: String,
    onUseClipboard: () -> Unit,
    onDownloadTriggered: (String) -> Unit
) {
    val context = LocalContext.current
    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(InstaPurple, InstaMagenta, InstaPink, InstaOrange)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Instagram-style Banner Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(gradientBrush)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Instagram Reel Saver",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Download Instagram Reels and Videos instantly using fastvideosave.net engine directly inside your app!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Clip board detection card
        if (detectedClipboardUrl.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUseClipboard() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = InstaMagenta,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Detected Reel in Clipboard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = detectedClipboardUrl,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PASTE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = InstaMagenta
                        )
                    }
                }
            }
        }

        // Download Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Paste Instagram Reel Link",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = onUrlChange,
                        placeholder = { Text("https://www.instagram.com/reel/...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InstaPink,
                            cursorColor = InstaPink
                        ),
                        trailingIcon = {
                            if (inputUrl.isNotEmpty()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear URL")
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    if (clipboard.hasPrimaryClip()) {
                                        onUrlChange(clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: "")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste")
                        }

                        Button(
                            onClick = {
                                if (inputUrl.isNotEmpty() && (inputUrl.contains("instagram.com") || inputUrl.startsWith("http"))) {
                                    onDownloadTriggered(inputUrl)
                                } else {
                                    android.widget.Toast.makeText(context, "Please enter a valid Instagram Link", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = InstaPink),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("btn_download_now")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Instructions Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "How to Download Reels",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = InstaPurple
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(InstaPink.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = InstaPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Open Instagram app & find your favorite Reel.", fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(InstaPink.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("2", color = InstaPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Click Share -> Copy Link.", fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(InstaPink.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = InstaPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Paste here & click Download. Our auto-engine handles the rest!", fontSize = 14.sp)
                    }
                }
            }
        }

        // Quick Link to Instagram Button
        item {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/"))
                        intent.setPackage("com.instagram.android")
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Instagram not installed, load in web browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/"))
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Open Instagram app", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BrowserScreen(
    webView: WebView,
    viewModel: DownloadViewModel
) {
    val isDownloading by viewModel.isDownloading.collectAsState()
    val progress by viewModel.downloadProgress.collectAsState()

    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("https://fastvideosave.net/") }

    // Periodically update browser navigation state
    LaunchedEffect(webView) {
        while (true) {
            canGoBack = webView.canGoBack()
            canGoForward = webView.canGoForward()
            currentUrl = webView.url ?: "https://fastvideosave.net/"
            kotlinx.coroutines.delay(500)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Download progress bar
        AnimatedVisibility(visible = isDownloading) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = InstaPink,
                    trackColor = InstaPink.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InstaPink.copy(alpha = 0.05f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Downloading video file...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InstaPink
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = InstaPink
                    )
                }
            }
        }

        // Browser custom navbar
        Card(
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { if (webView.canGoBack()) webView.goBack() },
                    enabled = canGoBack
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                }

                IconButton(
                    onClick = { if (webView.canGoForward()) webView.goForward() },
                    enabled = canGoForward
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go Forward")
                }

                IconButton(
                    onClick = { webView.reload() }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload Page")
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentUrl.replace("https://", ""),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { webView.loadUrl("https://fastvideosave.net/") }
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Go Home")
                }
            }
        }

        // The actual persistent WebView hosting fastvideosave.net
        AndroidView(
            factory = {
                // Ensure parent removal if re-attaching
                val parent = webView.parent as? android.view.ViewGroup
                parent?.removeView(webView)
                webView
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("webview_container")
        )
    }
}

@Composable
fun HistoryScreen(
    viewModel: DownloadViewModel,
    onPlayVideo: (String) -> Unit,
    onDeleteRequested: (DownloadItem) -> Unit
) {
    val downloads by viewModel.allDownloads.collectAsState()
    val context = LocalContext.current

    if (downloads.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Saved Reels",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your downloaded reels will be listed here. Copy a reel link and try downloading!",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Download History",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${downloads.size} reels",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InstaPink
                    )
                }
            }

            items(downloads, key = { it.id }) { item ->
                DownloadItemCard(
                    item = item,
                    onPlay = { onPlayVideo(item.videoPath) },
                    onShare = {
                        try {
                            val uri = Uri.parse(item.videoPath)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                
                                val contentUri = if (uri.scheme == "content") {
                                    uri
                                } else {
                                    val file = File(uri.path ?: "")
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                }
                                
                                putExtra(Intent.EXTRA_STREAM, contentUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Reel Video"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(context, "Could not share file: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDelete = { onDeleteRequested(item) }
                )
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Thumbnail with a Play Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Size: ${formatSize(item.sizeBytes)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDate(item.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = InstaMagenta,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerDialog(videoUriStr: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val videoUri = remember(videoUriStr) { Uri.parse(videoUriStr) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val mediaController = MediaController(ctx)
                        mediaController.setAnchorView(this)
                        setMediaController(mediaController)
                        setVideoURI(videoUri)
                        setOnPreparedListener { start() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f) // Reels are vertical 9:16
            )

            // Topbar layout overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playing Reel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Video",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun formatSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "Unknown size"
    val kb = sizeBytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.US, "%.2f MB", mb)
    } else {
        String.format(Locale.US, "%.2f KB", kb)
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(date)
}
