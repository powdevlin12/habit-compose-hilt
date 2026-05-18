package com.dttrn.habit_tracking.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Tạo và chia sẻ ảnh streak đẹp cho thói quen.
 * Dùng Canvas API để vẽ card theo phong cách gradient hiện đại.
 */
object StreakShareHelper {

    fun createAndShareStreakCard(
        context: Context,
        habitName: String,
        habitEmoji: String,
        habitColorHex: String,
        currentStreak: Int,
        longestStreak: Int,
        totalLogs: Int
    ) {
        val bitmap = createStreakBitmap(
            habitName = habitName,
            habitEmoji = habitEmoji,
            habitColorHex = habitColorHex,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalLogs = totalLogs
        )

        val file = saveBitmapToCache(context, bitmap)
        if (file != null) {
            shareImageFile(context, file)
        }
    }

    private fun createStreakBitmap(
        habitName: String,
        habitEmoji: String,
        habitColorHex: String,
        currentStreak: Int,
        longestStreak: Int,
        totalLogs: Int
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Parse color
        val habitColor = try {
            android.graphics.Color.parseColor(habitColorHex)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#4CAF50")
        }
        val r = android.graphics.Color.red(habitColor)
        val g = android.graphics.Color.green(habitColor)
        val b = android.graphics.Color.blue(habitColor)

        // Background gradient (dark base)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(
                android.graphics.Color.rgb(15, 15, 20),
                android.graphics.Color.rgb(
                    (r * 0.15).toInt().coerceIn(20, 60),
                    (g * 0.15).toInt().coerceIn(20, 60),
                    (b * 0.15).toInt().coerceIn(20, 60)
                )
            ),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Decorative circles
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = habitColor
            alpha = 20
        }
        canvas.drawCircle(width * 0.85f, height * 0.15f, 280f, circlePaint)
        circlePaint.alpha = 12
        canvas.drawCircle(width * 0.1f, height * 0.8f, 200f, circlePaint)

        // Card background
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(180, 25, 28, 35)
        }
        val cardRect = RectF(60f, 60f, width - 60f, height - 60f)
        canvas.drawRoundRect(cardRect, 60f, 60f, cardPaint)

        // Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = habitColor
            alpha = 80
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 60f, 60f, borderPaint)

        // Emoji (centered top)
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 160f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(habitEmoji, width / 2f, 310f, emojiPaint)

        // Habit name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (habitName.length > 20) habitName.take(20) + "…" else habitName,
            width / 2f, 400f, namePaint
        )

        // Streak fire label
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = habitColor
            textSize = 44f
            textAlign = Paint.Align.CENTER
            alpha = 220
        }
        canvas.drawText("🔥 STREAK HIỆN TẠI", width / 2f, 500f, labelPaint)

        // Big streak number
        val streakNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = habitColor
            textSize = 240f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$currentStreak", width / 2f, 720f, streakNumPaint)

        // "ngày liên tiếp"
        val daysPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 52f
            textAlign = Paint.Align.CENTER
            alpha = 200
        }
        canvas.drawText("ngày liên tiếp", width / 2f, 790f, daysPaint)

        // Divider
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = habitColor
            alpha = 60
            strokeWidth = 2f
        }
        canvas.drawLine(160f, 830f, width - 160f, 830f, divPaint)

        // Stats row: Longest streak | Total logs
        val statValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 68f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.CENTER
            alpha = 160
        }

        // Left stat: Longest streak
        canvas.drawText("🏆 $longestStreak", width / 4f, 910f, statValPaint)
        canvas.drawText("Streak dài nhất", width / 4f, 960f, statLabelPaint)

        // Right stat: Total logs
        canvas.drawText("📅 $totalLogs", width * 3 / 4f, 910f, statValPaint)
        canvas.drawText("Tổng check-in", width * 3 / 4f, 960f, statLabelPaint)

        // Branding
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 32f
            textAlign = Paint.Align.CENTER
            alpha = 80
        }
        canvas.drawText("Habit Journey Tracker", width / 2f, 1020f, brandPaint)

        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File? {
        return try {
            val dir = File(context.cacheDir, "share_images").apply { mkdirs() }
            val file = File(dir, "streak_share.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun shareImageFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Tôi đang duy trì streak với Habit Journey! 🔥")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Chia sẻ streak"))
    }
}
