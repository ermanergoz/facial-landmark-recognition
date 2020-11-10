package app.teknikcell.facerecognition.overlay

/**
 * Maps image coordinates onto the view the same way the camera preview is shown:
 * scaled to fill the view, centered, and mirrored horizontally for the front camera.
 */
class ImageToViewMapper(
    private val viewWidth: Float,
    viewHeight: Float,
    imageWidth: Float,
    imageHeight: Float,
    private val isMirrored: Boolean
) {

    private val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
    private val offsetX = (viewWidth - imageWidth * scale) / 2f
    private val offsetY = (viewHeight - imageHeight * scale) / 2f

    fun mapX(x: Float): Float {
        val mapped = x * scale + offsetX
        return if (isMirrored) viewWidth - mapped else mapped
    }

    fun mapY(y: Float): Float = y * scale + offsetY
}
