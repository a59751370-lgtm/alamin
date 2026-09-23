package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_dials")
data class QuickDialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val iconKey: String = "",
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
)
