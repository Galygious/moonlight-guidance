package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import org.arcanaforge.app.domain.image.ImageSource

@Entity(tableName = "stored_images")
data class StoredImageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "local_path") val localPath: String,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String?,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    val width: Int,
    val height: Int,
    val source: ImageSource,
    val prompt: String? = null,
    val provider: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
)
