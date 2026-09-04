package com.example

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DownloadViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: DownloadViewModel
    private val sharedUrlState = mutableStateOf("")

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
            if (id != -1L) {
                viewModel.handleDownloadCompleted(id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        viewModel = ViewModelProvider(this)[DownloadViewModel::class.java]

        // Handle cold start sharing intent
        sharedUrlState.value = extractInstagramUrl(intent)

        // Register BroadcastReceiver for system download completion events
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(downloadReceiver, intentFilter)
        }

        setContent {
            MyApplicationTheme {
                HomeScreen(
                    viewModel = viewModel,
                    initialSharedUrl = sharedUrlState.value,
                    onSharedUrlProcessed = { sharedUrlState.value = "" },
                    onCloseApp = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val extracted = extractInstagramUrl(intent)
        if (extracted.isNotEmpty()) {
            sharedUrlState.value = extracted
        }
    }

    private fun extractInstagramUrl(intent: Intent?): String {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            val urlPattern = "(https?://[\\w-]+(\\.[\\w-]+)+(/\\S*)?)".toRegex()
            val matchResult = urlPattern.find(sharedText)
            val extractedUrl = matchResult?.value ?: ""
            if (extractedUrl.contains("instagram.com")) {
                return extractedUrl
            }
        }
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(downloadReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
