package app.teknikcell.facerecognition.detection

/**
 * Receives the outcome of analyzing one camera frame.
 *
 * [imageWidth] and [imageHeight] describe the frame upright, after the camera
 * rotation is applied, so they share the coordinate space of the results.
 */
interface DetectionListener<T> {

    fun onDetectionSuccess(results: T, imageWidth: Int, imageHeight: Int)

    fun onDetectionFailure(e: Exception)
}
