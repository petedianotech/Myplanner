package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Internal schema marker so Room has a valid initial database.
 * Not used by application features. Will remain or be replaced as
 * real entities are introduced in later phases.
 */
@Entity(tableName = "schema_meta")
data class SchemaMeta(
    @PrimaryKey val id: Int = 1,
    val versionLabel: String = "phase1"
)
