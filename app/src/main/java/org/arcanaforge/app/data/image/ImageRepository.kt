package org.arcanaforge.app.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.arcanaforge.app.core.database.dao.StoredImageDao
import org.arcanaforge.app.core.database.entity.StoredImageEntity
import org.arcanaforge.app.domain.image.ImageSource

interface ImageRepository {
    fun observeImage(id: String): Flow<StoredImageEntity?>
    fun observeImages(ids: List<String>): Flow<List<StoredImageEntity>>
    suspend fun getImage(id: String): StoredImageEntity?
    suspend fun importPickedImage(uri: Uri): StoredImageEntity
}

class LocalImageRepository(
    private val context: Context,
    private val storedImageDao: StoredImageDao,
) : ImageRepository {
    override fun observeImage(id: String): Flow<StoredImageEntity?> = storedImageDao.observeImage(id)

    override fun observeImages(ids: List<String>): Flow<List<StoredImageEntity>> =
        if (ids.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            storedImageDao.observeImages(ids)
        }

    override suspend fun getImage(id: String): StoredImageEntity? = storedImageDao.getImage(id)

    override suspend fun importPickedImage(uri: Uri): StoredImageEntity = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val mimeType = context.contentResolver.getType(uri) ?: "image/*"
        val extension = mimeTypeToExtension(mimeType)
        val imageDir = File(context.filesDir, IMAGE_DIR).apply { mkdirs() }
        val thumbnailDir = File(context.filesDir, THUMBNAIL_DIR).apply { mkdirs() }
        val imageFile = File(imageDir, "$id.$extension")
        val thumbnailFile = File(thumbnailDir, "$id.webp")

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Selected image could not be opened." }
            imageFile.outputStream().use { output -> input.copyTo(output) }
        }

        val bitmap = requireNotNull(BitmapFactory.decodeFile(imageFile.absolutePath)) {
            "Selected file is not a readable image."
        }
        val thumbnail = bitmap.scaledToFit(maxSize = 360)
        thumbnailFile.outputStream().use { output ->
            thumbnail.compress(Bitmap.CompressFormat.WEBP, 84, output)
        }
        if (thumbnail !== bitmap) {
            thumbnail.recycle()
        }

        val storedImage = StoredImageEntity(
            id = id,
            localPath = imageFile.absolutePath,
            thumbnailPath = thumbnailFile.absolutePath,
            mimeType = mimeType,
            width = bitmap.width,
            height = bitmap.height,
            source = ImageSource.Uploaded,
            createdAt = Instant.now(),
        )
        bitmap.recycle()
        storedImageDao.insert(storedImage)
        storedImage
    }

    private fun Bitmap.scaledToFit(maxSize: Int): Bitmap {
        val largestSide = maxOf(width, height)
        if (largestSide <= maxSize) return this

        val scale = maxSize.toFloat() / largestSide.toFloat()
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    private fun mimeTypeToExtension(mimeType: String): String = when (mimeType.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }

    private companion object {
        const val IMAGE_DIR = "images/cards"
        const val THUMBNAIL_DIR = "images/thumbnails"
    }
}
