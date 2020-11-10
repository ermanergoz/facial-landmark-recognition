package app.teknikcell.facerecognition.detection

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import java.io.Closeable

/**
 * Feeds camera frames to an ML Kit detector and reports the results to [listener].
 * Subclasses only provide the detector call and release it in [close].
 */
abstract class BaseImageAnalyzer<T>(
    private val listener: DetectionListener<T>
) : ImageAnalysis.Analyzer, Closeable {

    protected abstract fun detectInImage(image: InputImage): Task<T>

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val isRotatedSideways = rotationDegrees == 90 || rotationDegrees == 270
        val imageWidth = if (isRotatedSideways) imageProxy.height else imageProxy.width
        val imageHeight = if (isRotatedSideways) imageProxy.width else imageProxy.height

        detectInImage(InputImage.fromMediaImage(mediaImage, rotationDegrees))
            .addOnSuccessListener { results ->
                listener.onDetectionSuccess(results, imageWidth, imageHeight)
            }
            .addOnFailureListener { e -> listener.onDetectionFailure(e) }
            .addOnCompleteListener { imageProxy.close() }
    }
}
