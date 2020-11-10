package app.teknikcell.facerecognition.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import kotlin.math.max
import kotlin.math.min

class FaceContourGraphic(private val face: Face) : GraphicOverlay.Graphic {

    private val boxPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }
    private val pointPaint = Paint().apply {
        color = Color.WHITE
    }
    private val contourPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }
    private val contourPath = Path()

    override fun draw(canvas: Canvas, mapper: ImageToViewMapper) {
        drawBoundingBox(canvas, mapper)
        drawContourPoints(canvas, mapper)
        CONTOUR_COLORS.forEach { (contourType, color) ->
            drawContour(canvas, mapper, contourType, color)
        }
    }

    private fun drawBoundingBox(canvas: Canvas, mapper: ImageToViewMapper) {
        val box = face.boundingBox
        val left = mapper.mapX(box.left.toFloat())
        val right = mapper.mapX(box.right.toFloat())
        canvas.drawRect(
            min(left, right),
            mapper.mapY(box.top.toFloat()),
            max(left, right),
            mapper.mapY(box.bottom.toFloat()),
            boxPaint
        )
    }

    private fun drawContourPoints(canvas: Canvas, mapper: ImageToViewMapper) {
        face.allContours.forEach { contour ->
            contour.points.forEach { point ->
                canvas.drawCircle(mapper.mapX(point.x), mapper.mapY(point.y), POINT_RADIUS, pointPaint)
            }
        }
    }

    private fun drawContour(
        canvas: Canvas,
        mapper: ImageToViewMapper,
        contourType: Int,
        @ColorInt color: Int
    ) {
        val points = face.getContour(contourType)?.points ?: return
        if (points.isEmpty()) return

        contourPath.reset()
        points.forEachIndexed { index, point ->
            val x = mapper.mapX(point.x)
            val y = mapper.mapY(point.y)
            if (index == 0) contourPath.moveTo(x, y) else contourPath.lineTo(x, y)
        }
        contourPaint.color = color
        canvas.drawPath(contourPath, contourPaint)
    }

    companion object {
        private const val POINT_RADIUS = 4.0f
        private const val STROKE_WIDTH = 5.0f

        private val CONTOUR_COLORS = listOf(
            FaceContour.FACE to Color.BLUE,
            FaceContour.LEFT_EYEBROW_TOP to Color.RED,
            FaceContour.LEFT_EYE to Color.BLACK,
            FaceContour.LEFT_EYEBROW_BOTTOM to Color.CYAN,
            FaceContour.RIGHT_EYE to Color.DKGRAY,
            FaceContour.RIGHT_EYEBROW_BOTTOM to Color.GRAY,
            FaceContour.RIGHT_EYEBROW_TOP to Color.GREEN,
            FaceContour.NOSE_BOTTOM to Color.LTGRAY,
            FaceContour.NOSE_BRIDGE to Color.MAGENTA,
            FaceContour.LOWER_LIP_BOTTOM to Color.WHITE,
            FaceContour.LOWER_LIP_TOP to Color.YELLOW,
            FaceContour.UPPER_LIP_BOTTOM to Color.GREEN,
            FaceContour.UPPER_LIP_TOP to Color.CYAN
        )
    }
}
