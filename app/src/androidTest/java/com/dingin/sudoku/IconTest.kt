package com.dingin.sudoku

import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class IconTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun launcherProvidesAdaptiveLayersAndMonochrome() {
        val icon = context.getDrawable(context.applicationInfo.icon)
        assertTrue(icon is AdaptiveIconDrawable)
        icon as AdaptiveIconDrawable
        assertNotNull(icon.background); assertNotNull(icon.foreground)
        if (Build.VERSION.SDK_INT >= 33) assertNotNull(icon.monochrome)
    }
    @Test fun authoredForegroundStaysInsideSafeZone() {
        val bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
        val foreground = context.getDrawable(R.drawable.ic_dingin_foreground)!!
        foreground.setBounds(0, 0, 108, 108); foreground.draw(Canvas(bitmap))
        val visible = (0..107).flatMap { y -> (0..107).filter { x -> Color.alpha(bitmap.getPixel(x, y)) > 0 }.map { x -> x to y } }
        assertTrue(visible.isNotEmpty())
        assertTrue(visible.all { (x, y) -> x in 21..86 && y in 21..86 })
        assertTrue(visible.maxOf { it.first } - visible.minOf { it.first } + 1 >= 48)
        assertTrue(visible.maxOf { it.second } - visible.minOf { it.second } + 1 >= 48)
    }
    @Test fun maskPreviewsKeepMonogramVisible() {
        val dir = File(context.filesDir, "design-previews").apply { mkdirs() }
        for (mask in listOf("circle", "rounded-square", "squircle", "monochrome")) {
            val b = Bitmap.createBitmap(432, 432, Bitmap.Config.ARGB_8888); val canvas = Canvas(b)
            val shape = Path()
            when (mask) {
                "circle", "monochrome" -> shape.addCircle(216f, 216f, 210f, Path.Direction.CW)
                "rounded-square" -> shape.addRoundRect(6f, 6f, 426f, 426f, 80f, 80f, Path.Direction.CW)
                else -> {
                    for (step in 0..360) {
                        val a = Math.toRadians(step.toDouble())
                        val c = kotlin.math.cos(a); val s = kotlin.math.sin(a)
                        val x = (216 + 210 * kotlin.math.sign(c) * kotlin.math.sqrt(kotlin.math.abs(c))).toFloat()
                        val y = (216 + 210 * kotlin.math.sign(s) * kotlin.math.sqrt(kotlin.math.abs(s))).toFloat()
                        if (step == 0) shape.moveTo(x, y) else shape.lineTo(x, y)
                    }
                    shape.close()
                }
            }
            canvas.clipPath(shape)
            canvas.drawColor(if (mask == "monochrome") Color.rgb(215, 234, 227) else Color.rgb(25, 89, 79))
            val drawable = context.getDrawable(if (mask == "monochrome") R.drawable.ic_dingin_monochrome else R.drawable.ic_dingin_foreground)!!.mutate()
            if (mask == "monochrome") drawable.setTint(Color.rgb(25, 89, 79))
            // Launcher layers occupy 108dp while the visible mask covers the central 72dp.
            drawable.setBounds(-108, -108, 540, 540); drawable.draw(canvas)
            val background = if (mask == "monochrome") Color.rgb(215, 234, 227) else Color.rgb(25, 89, 79)
            assertNotEquals(background, b.getPixel(90, 216))
            assertNotEquals(background, b.getPixel(216, 216))
            FileOutputStream(File(dir, "icon-$mask.png")).use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
