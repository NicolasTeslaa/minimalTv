package com.minimaltv.launcher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.LinearLayout
import com.minimaltv.launcher.R

class GradientBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setWillNotDraw(false)
        setBackgroundColor(context.getColor(R.color.background))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        paint.shader = RadialGradient(
            width * 0.18f, height * 0.92f, width * 0.62f,
            intArrayOf(context.getColor(R.color.glow_blue), context.getColor(R.color.transparent)),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, height * 0.55f, width * 0.78f, height, paint)

        paint.shader = RadialGradient(
            width * 0.93f, height * 0.1f, width * 0.72f,
            intArrayOf(context.getColor(R.color.glow_purple), context.getColor(R.color.transparent)),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(width * 0.45f, 0f, width, height * 0.72f, paint)

        val topWave = Path().apply {
            moveTo(-width * 0.08f, -height * 0.16f)
            cubicTo(width * 0.2f, height * 0.02f, width * 0.3f, height * 0.38f, width * 0.64f, height * 0.25f)
            cubicTo(width * 0.84f, height * 0.18f, width * 0.98f, -height * 0.04f, width * 1.08f, -height * 0.08f)
            lineTo(width * 1.08f, height * 0.32f)
            cubicTo(width * 0.86f, height * 0.46f, width * 0.58f, height * 0.27f, width * 0.32f, height * 0.34f)
            cubicTo(width * 0.08f, height * 0.4f, -width * 0.04f, height * 0.18f, -width * 0.08f, -height * 0.16f)
            close()
        }
        paint.shader = LinearGradient(
            0f, 0f, width, height * 0.45f,
            intArrayOf(context.getColor(R.color.wave_blue), context.getColor(R.color.wave_purple)),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(topWave, paint)

        val bottomWave = Path().apply {
            moveTo(-width * 0.08f, height * 0.78f)
            cubicTo(width * 0.1f, height * 1.05f, width * 0.34f, height * 1.08f, width * 0.58f, height * 0.91f)
            cubicTo(width * 0.82f, height * 0.74f, width * 1.0f, height * 0.82f, width * 1.08f, height * 0.55f)
            lineTo(width * 1.08f, height * 1.08f)
            lineTo(-width * 0.08f, height * 1.08f)
            close()
        }
        paint.shader = LinearGradient(
            0f, height, width, height * 0.68f,
            intArrayOf(context.getColor(R.color.wave_blue), context.getColor(R.color.wave_purple)),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawPath(bottomWave, paint)
        paint.shader = null
    }
}
