package app.teknikcell.facerecognition.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Binds the camera preview and frame analysis to [lifecycleOwner].
 * Frames are passed to [analyzer] on a background thread.
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val analyzer: ImageAnalysis.Analyzer,
    lensFacing: Int
) {

    var lensFacing: Int = lensFacing
        private set

    val isFrontFacing: Boolean
        get() = lensFacing == CameraSelector.LENS_FACING_FRONT

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    fun start() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            Runnable {
                cameraProvider = cameraProviderFuture.get()
                bindUseCases()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun switchCamera() {
        lensFacing =
            if (isFrontFacing) CameraSelector.LENS_FACING_BACK
            else CameraSelector.LENS_FACING_FRONT
        bindUseCases()
    }

    fun zoomBy(scaleFactor: Float) {
        val camera = camera ?: return
        val zoomState = camera.cameraInfo.zoomState.value ?: return
        val zoomRatio = (zoomState.zoomRatio * scaleFactor)
            .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
        camera.cameraControl.setZoomRatio(zoomRatio)
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }

    private fun bindUseCases() {
        val cameraProvider = cameraProvider ?: return

        val preview = Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(cameraExecutor, analyzer) }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            preview.setSurfaceProvider(previewView.createSurfaceProvider())
        } catch (e: Exception) {
            Log.e(TAG, "Binding camera use cases failed", e)
        }
    }

    private companion object {
        const val TAG = "CameraManager"
    }
}
