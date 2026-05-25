package org.arcanaforge.app.ui.screens.readings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import org.arcanaforge.app.domain.reading.ReadingOrientation

private const val ShareImageWidth = 1200
private const val ShareImageMinimumHeight = 1000

fun shareReadingImage(
    context: Context,
    state: ReadingDetailUiState,
) {
    val file = renderReadingImage(context, state)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share reading image"))
}

private fun renderReadingImage(
    context: Context,
    state: ReadingDetailUiState,
): File {
    val reading = requireNotNull(state.reading) { "Reading is not loaded." }
    val width = ShareImageWidth
    val horizontalPadding = 72f
    val contentWidth = width - horizontalPadding * 2
    val columns = gridColumnCount(state.items.size)
    val columnGap = if (columns >= 3) 28f else 36f
    val rowGap = 48f
    val gridWidth = contentWidth - columnGap * (columns - 1)
    val cellWidth = when (columns) {
        1 -> min(620f, contentWidth)
        else -> gridWidth / columns
    }
    val cardImageHeight = cellWidth * 1.58f

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(38, 34, 30)
        textSize = 46f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(82, 76, 68)
        textSize = 25f
    }
    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(38, 34, 30)
        textSize = 30f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(55, 50, 45)
        textSize = 24f
    }
    val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(96, 88, 78)
        textSize = 21f
    }

    val rowCount = if (state.items.isEmpty()) 0 else (state.items.size + columns - 1) / columns
    val rowHeights = (0 until rowCount).map { row ->
        state.items
            .drop(row * columns)
            .take(columns)
            .maxOfOrNull { item ->
                measureCardTileHeight(
                    item = item,
                    cellWidth = cellWidth,
                    cardImageHeight = cardImageHeight,
                    sectionPaint = sectionPaint,
                    bodyPaint = bodyPaint,
                    smallPaint = smallPaint,
                )
            } ?: 0f
    }
    val measuredHeight = measureImageHeight(
        state = state,
        width = width,
        horizontalPadding = horizontalPadding,
        rowHeights = rowHeights,
        rowGap = rowGap,
        titlePaint = titlePaint,
        subtitlePaint = subtitlePaint,
        sectionPaint = sectionPaint,
        bodyPaint = bodyPaint,
    )
    val bitmap = Bitmap.createBitmap(width, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.rgb(250, 247, 239))

    var y = 78f
    y = drawWrappedText(canvas, reading.title, horizontalPadding, y, width - horizontalPadding * 2, titlePaint, 56f)
    y += 12f
    y = drawWrappedText(
        canvas = canvas,
        text = "${state.deckName} - ${state.layoutName}",
        x = horizontalPadding,
        y = y,
        maxWidth = width - horizontalPadding * 2,
        paint = subtitlePaint,
        lineHeight = 34f,
    )
    if (reading.question.isNotBlank()) {
        y += 28f
        y = drawWrappedText(canvas, "Question", horizontalPadding, y, width - horizontalPadding * 2, sectionPaint, 38f)
        y += 4f
        y = drawWrappedText(canvas, reading.question, horizontalPadding, y, width - horizontalPadding * 2, bodyPaint, 34f)
    }
    y += 44f

    repeat(rowCount) { row ->
        if (row > 0) {
            y += rowGap
        }
        val rowTop = y
        val rowItems = state.items.drop(row * columns).take(columns)
        val rowWidth = cellWidth * rowItems.size + columnGap * (rowItems.size - 1).coerceAtLeast(0)
        val rowStartX = horizontalPadding + (contentWidth - rowWidth) / 2f
        rowItems.forEachIndexed { column, item ->
            val x = rowStartX + column * (cellWidth + columnGap)
            drawCardTile(
                canvas = canvas,
                item = item,
                x = x,
                y = rowTop,
                cellWidth = cellWidth,
                cardImageHeight = cardImageHeight,
                sectionPaint = sectionPaint,
                bodyPaint = bodyPaint,
                smallPaint = smallPaint,
            )
        }
        y += rowHeights[row]
    }

    if (state.readingNotes.isNotBlank()) {
        y += 28f
        y = drawWrappedText(canvas, "Reading Notes", horizontalPadding, y, width - horizontalPadding * 2, sectionPaint, 38f)
        drawWrappedText(canvas, state.readingNotes, horizontalPadding, y + 4f, width - horizontalPadding * 2, bodyPaint, 34f, maxLines = 8)
    }

    val directory = File(context.cacheDir, "shared_readings").apply { mkdirs() }
    val file = File(directory, "moonlight-reading-${reading.id}.png")
    file.outputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    bitmap.recycle()
    return file
}

private fun measureImageHeight(
    state: ReadingDetailUiState,
    width: Int,
    horizontalPadding: Float,
    rowHeights: List<Float>,
    rowGap: Float,
    titlePaint: Paint,
    subtitlePaint: Paint,
    sectionPaint: Paint,
    bodyPaint: Paint,
): Int {
    val reading = state.reading ?: return ShareImageMinimumHeight
    var y = 78f
    y += measureWrappedText(reading.title, width - horizontalPadding * 2, titlePaint, 56f)
    y += measureWrappedText("${state.deckName} - ${state.layoutName}", width - horizontalPadding * 2, subtitlePaint, 34f) + 12f
    if (reading.question.isNotBlank()) {
        y += 32f
        y += measureWrappedText("Question", width - horizontalPadding * 2, sectionPaint, 38f)
        y += measureWrappedText(reading.question, width - horizontalPadding * 2, bodyPaint, 34f)
    }
    y += 44f
    rowHeights.forEachIndexed { index, rowHeight ->
        if (index > 0) {
            y += rowGap
        }
        y += rowHeight
    }
    if (state.readingNotes.isNotBlank()) {
        y += 36f
        y += measureWrappedText("Reading Notes", width - horizontalPadding * 2, sectionPaint, 38f)
        y += measureWrappedText(state.readingNotes, width - horizontalPadding * 2, bodyPaint, 34f, maxLines = 8)
    }
    return (y + 96f).toInt().coerceAtLeast(ShareImageMinimumHeight)
}

private fun drawCardTile(
    canvas: Canvas,
    item: ReadingDetailItem,
    x: Float,
    y: Float,
    cellWidth: Float,
    cardImageHeight: Float,
    sectionPaint: Paint,
    bodyPaint: Paint,
    smallPaint: Paint,
) {
    var currentY = y
    currentY = drawWrappedText(canvas, item.slot.title, x, currentY, cellWidth, sectionPaint, 36f, maxLines = 2)
    currentY += 14f
    drawCardImage(canvas, item, x, currentY, cellWidth, cardImageHeight)
    currentY += cardImageHeight + 14f
    currentY = drawWrappedText(
        canvas = canvas,
        text = "${item.card.title} (${item.readingCard.orientation.displayName()})",
        x = x,
        y = currentY,
        maxWidth = cellWidth,
        paint = bodyPaint,
        lineHeight = 32f,
        maxLines = 3,
    )
    val meaning = item.meaningForOrientation()
    if (meaning.isNotBlank()) {
        currentY = drawWrappedText(canvas, meaning, x, currentY + 10f, cellWidth, smallPaint, 29f)
    }
    if (item.readingCard.userNote.isNotBlank()) {
        drawWrappedText(canvas, "Note: ${item.readingCard.userNote}", x, currentY + 8f, cellWidth, smallPaint, 29f)
    }
}

private fun measureCardTileHeight(
    item: ReadingDetailItem,
    cellWidth: Float,
    cardImageHeight: Float,
    sectionPaint: Paint,
    bodyPaint: Paint,
    smallPaint: Paint,
): Float {
    var height = measureWrappedText(item.slot.title, cellWidth, sectionPaint, 36f, maxLines = 2)
    height += 14f + cardImageHeight + 14f
    height += measureWrappedText(
        "${item.card.title} (${item.readingCard.orientation.displayName()})",
        cellWidth,
        bodyPaint,
        32f,
        maxLines = 3,
    )
    val meaning = item.meaningForOrientation()
    if (meaning.isNotBlank()) {
        height += 10f + measureWrappedText(meaning, cellWidth, smallPaint, 29f)
    }
    if (item.readingCard.userNote.isNotBlank()) {
        height += 8f + measureWrappedText("Note: ${item.readingCard.userNote}", cellWidth, smallPaint, 29f)
    }
    return height
}

private fun gridColumnCount(itemCount: Int): Int {
    return when (itemCount) {
        0, 1 -> 1
        2 -> 2
        3 -> 3
        4 -> 2
        else -> ceil(sqrt(itemCount.toDouble())).toInt()
    }
}

private fun drawCardImage(
    canvas: Canvas,
    item: ReadingDetailItem,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) {
    val rect = RectF(x, y, x + width, y + height)
    val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(232, 225, 213)
    }
    canvas.drawRoundRect(rect, 18f, 18f, framePaint)
    val imagePath = item.image?.thumbnailPath ?: item.image?.localPath
    val bitmap = imagePath?.let { BitmapFactory.decodeFile(it) }
    if (bitmap == null) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(110, 101, 90)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("No image", rect.centerX(), rect.centerY(), paint)
        return
    }

    val scale = min(width / bitmap.width, height / bitmap.height)
    val scaledWidth = bitmap.width * scale
    val scaledHeight = bitmap.height * scale
    val left = x + (width - scaledWidth) / 2f
    val top = y + (height - scaledHeight) / 2f
    canvas.save()
    canvas.clipRect(rect)
    if (item.readingCard.orientation == ReadingOrientation.Reversed) {
        canvas.rotate(180f, rect.centerX(), rect.centerY())
    }
    canvas.drawBitmap(bitmap, null, RectF(left, top, left + scaledWidth, top + scaledHeight), null)
    canvas.restore()
    bitmap.recycle()
}

private fun drawWrappedText(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
    lineHeight: Float,
    maxLines: Int = Int.MAX_VALUE,
): Float {
    var currentY = y
    wrapText(text, maxWidth, paint, maxLines).forEach { line ->
        currentY += lineHeight
        canvas.drawText(line, x, currentY, paint)
    }
    return currentY
}

private fun measureWrappedText(
    text: String,
    maxWidth: Float,
    paint: Paint,
    lineHeight: Float,
    maxLines: Int = Int.MAX_VALUE,
): Float = wrapText(text, maxWidth, paint, maxLines).size * lineHeight

private fun wrapText(
    text: String,
    maxWidth: Float,
    paint: Paint,
    maxLines: Int,
): List<String> {
    if (text.isBlank()) return emptyList()
    val lines = mutableListOf<String>()
    text.lines().forEach { rawLine ->
        var current = ""
        rawLine.split(Regex("\\s+")).filter { it.isNotBlank() }.forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotBlank()) lines += current
                current = word
            }
            if (lines.size == maxLines) return lines.withEllipsis(maxLines)
        }
        if (current.isNotBlank()) lines += current
        if (lines.size >= maxLines) return lines.withEllipsis(maxLines)
    }
    return lines
}

private fun List<String>.withEllipsis(maxLines: Int): List<String> =
    take(maxLines).toMutableList().also { lines ->
        if (lines.isNotEmpty()) {
            lines[lines.lastIndex] = lines.last().trimEnd('.', ',', ';', ':') + "..."
        }
    }

private fun ReadingDetailItem.meaningForOrientation(): String =
    if (readingCard.orientation == ReadingOrientation.Reversed) {
        card.reversedMeaning.ifBlank { card.uprightMeaning }
    } else {
        card.uprightMeaning
    }

private fun ReadingOrientation.displayName(): String =
    name.lowercase().replaceFirstChar { it.titlecase() }
