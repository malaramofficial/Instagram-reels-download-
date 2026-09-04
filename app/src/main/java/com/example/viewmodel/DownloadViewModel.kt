package com.example.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadItem
import com.example.data.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DownloadRepository
    val allDownloads: StateFlow<List<DownloadItem>>

    // Track active downloading status/progress
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    // Map of downloadId to sourceUrl for database insertion after completion
    private val activeDownloadsMap = mutableMapOf<Long, Pair<String, String>>() // downloadId -> (sourceUrl, title)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DownloadRepository(database.downloadDao())
        allDownloads = repository.allDownloads.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    /**
     * Enqueues a download with the system DownloadManager
     */
    fun enqueueDownload(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                
                val uri = Uri.parse(url)
                val fileName = "Reel_${System.currentTimeMillis()}.mp4"

                val request = DownloadManager.Request(uri).apply {
                    setTitle(fileName)
                    setDescription("Downloading Instagram Reel via FastVideoSave")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    
                    // Set destination to public Downloads directory
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ReelsDownloader/$fileName")
                    
                    // Attempt to set mime type
                    val cleanMime = if (mimetype.isNullOrEmpty() || mimetype == "application/octet-stream") {
                        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "video/mp4"
                    } else {
                        mimetype
                    }
                    setMimeType(cleanMime)
                    
                    if (!userAgent.isNullOrEmpty()) {
                        addRequestHeader("User-Agent", userAgent)
                    }
                }

                _isDownloading.value = true
                _downloadProgress.value = 0.1f

                val downloadId = downloadManager.enqueue(request)
                activeDownloadsMap[downloadId] = Pair(url, fileName)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download started: $fileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                _isDownloading.value = false
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        "Failed to start download: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Called when DownloadManager completes a download
     */
    fun handleDownloadCompleted(downloadId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val query = DownloadManager.Query().setFilterById(downloadId)
            var cursor: Cursor? = null
            try {
                cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusColumn)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val localUriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val localUriStr = cursor.getString(localUriColumn)
                        
                        val sizeColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val sizeBytes = cursor.getLong(sizeColumn)

                        val activeInfo = activeDownloadsMap[downloadId]
                        val sourceUrl = activeInfo?.first ?: ""
                        val title = activeInfo?.second ?: "Downloaded Reel"

                        // Insert into Room DB
                        val item = DownloadItem(
                            title = title,
                            sourceUrl = sourceUrl,
                            videoPath = localUriStr ?: "",
                            sizeBytes = sizeBytes,
                            thumbnailPath = null // Will resolve thumbnails dynamically or overlay play icon
                        )
                        repository.insert(item)
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Saved to History: $title", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Download failed or interrupted", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
                activeDownloadsMap.remove(downloadId)
                _isDownloading.value = false
                _downloadProgress.value = 0f
            }
        }
    }

    fun deleteDownload(item: DownloadItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            try {
                // Delete local file
                val uri = Uri.parse(item.videoPath)
                if (uri.scheme == "file") {
                    val file = File(uri.path ?: "")
                    if (file.exists()) {
                        file.delete()
                    }
                } else if (uri.scheme == "content") {
                    // Try to delete via ContentResolver
                    context.contentResolver.delete(uri, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.delete(item)
        }
    }
}
