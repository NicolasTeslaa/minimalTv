package com.minimaltv.launcher.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import android.widget.ImageView
import java.util.concurrent.ExecutorService

class AppIconLoader(private val context: Context, private val executor: ExecutorService) {
    private val size = (48 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
    private val cache = object : LruCache<String, Bitmap>(2 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun load(packageName: String, imageView: ImageView) {
        imageView.tag = packageName
        cache.get(packageName)?.let {
            imageView.setImageBitmap(it)
            return
        }
        imageView.setImageDrawable(null)
        executor.execute {
            val bitmap = try {
                val drawable = context.packageManager.getApplicationIcon(packageName).mutate()
                Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { result ->
                    drawable.setBounds(0, 0, size, size)
                    drawable.draw(Canvas(result))
                }
            } catch (_: Exception) {
                null
            }
            if (bitmap != null) {
                cache.put(packageName, bitmap)
                imageView.post {
                    if (imageView.tag == packageName) imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    fun clear() = cache.evictAll()
}
