package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val sourceUrl: String,
    val videoPath: String, // Path or URI to local video file
    val timestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L,
    val thumbnailPath: String? = null
)
