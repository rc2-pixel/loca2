package com.example.locationapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val filePath: String,        // 写真ファイルのパス
    val address: String,         // 住所
    val latitude: Double,        // 緯度
    val longitude: Double,       // 経度
    val altitude: Double,        // 標高
    val memo: String = "",       // メモ
    val timestamp: Long          // 撮影日時
)
