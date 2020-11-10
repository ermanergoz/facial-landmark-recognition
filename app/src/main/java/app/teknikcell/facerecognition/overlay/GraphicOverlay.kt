package app.teknikcell.facerecognition.overlay

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

/**
 * Draws [Graphic]s on top of the camera preview. Graphics work in image
 * coordinates and are mapped onto this view when drawn.
 */
class GraphicOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    interface Graphic {
        fun draw(canvas: Canvas, mapper: ImageToViewMapper)
    }

    private val lock = Any()
    private val graphics = mutableListOf<Graphic>()
    private var mapper: ImageToViewMapper? = null

    /** Replaces the current graphics with [newGraphics] found in an upright image of the given size. */
    fun show(newGraphics: List<Graphic>, imageWidth: Int, imageHeight: Int, isMirrored: Boolean) {
        val newMapper = ImageToViewMapper(
            width.toFloat(),
            height.toFloat(),
            imageWidth.toFloat(),
            imageHeight.toFloat(),
            isMirrored
        )
        synchronized(lock) {
            graphics.clear()
            graphics.addAll(newGraphics)
            mapper = newMapper
        }
        postInvalidate()
    }

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            val mapper = mapper ?: return
            graphics.forEach { it.draw(canvas, mapper) }
        }
    }
}
