package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String = "",
    val category: String = CATEGORY_OTHER,
    val status: String = STATUS_NEW,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val CATEGORY_APP = "app"
        const val CATEGORY_BUSINESS = "business"
        const val CATEGORY_CONTENT = "content"
        const val CATEGORY_SCHOOL = "school"
        const val CATEGORY_PERSONAL = "personal"
        const val CATEGORY_OTHER = "other"

        const val STATUS_NEW = "new"
        const val STATUS_WORKING = "working"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_ARCHIVED = "archived"
    }
}
