package app.teknikcell.facerecognition

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.teknikcell.facerecognition.camera.CameraManager
import app.teknikcell.facerecognition.detection.DetectionListener
import app.teknikcell.facerecognition.detection.FaceContourDetectionProcessor
import app.teknikcell.facerecognition.overlay.FaceContourGraphic
import app.teknikcell.facerecognition.overlay.GraphicOverlay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.face.Face

class MainActivity : AppCompatActivity() {

    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var faceDetectionProcessor: FaceContourDetectionProcessor
    private lateinit var cameraManager: CameraManager

    private val faceDetectionListener = object : DetectionListener<List<Face>> {
        override fun onDetectionSuccess(results: List<Face>, imageWidth: Int, imageHeight: Int) {
            graphicOverlay.show(
                results.map { FaceContourGraphic(it) },
                imageWidth,
                imageHeight,
                cameraManager.isFrontFacing
            )
        }

        override fun onDetectionFailure(e: Exception) {
            Log.e(TAG, "Face detection failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewView = findViewById<PreviewView>(R.id.previewView_finder)
        graphicOverlay = findViewById(R.id.graphicOverlay_finder)

        faceDetectionProcessor = FaceContourDetectionProcessor(faceDetectionListener)
        cameraManager = CameraManager(
            context = this,
            lifecycleOwner = this,
            previewView = previewView,
            analyzer = faceDetectionProcessor,
            lensFacing = savedInstanceState?.getInt(KEY_LENS_FACING, DEFAULT_LENS_FACING)
                ?: DEFAULT_LENS_FACING
        )

        findViewById<FloatingActionButton>(R.id.rotateCameraButton).setOnClickListener {
            graphicOverlay.clear()
            cameraManager.switchCamera()
        }
        setUpPinchToZoom(previewView)

        if (allPermissionsGranted()) {
            cameraManager.start()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_LENS_FACING, cameraManager.lensFacing)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.shutdown()
        faceDetectionProcessor.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE_PERMISSIONS) return

        if (allPermissionsGranted()) {
            cameraManager.start()
        } else {
            Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // PreviewView is a library view, so it cannot override performClick(); it is called on ACTION_UP.
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPinchToZoom(previewView: PreviewView) {
        val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                cameraManager.zoomBy(detector.scaleFactor)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(this, scaleListener)
        previewView.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) view.performClick()
            true
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val TAG = "MainActivity"
        const val KEY_LENS_FACING = "lens_facing"
        const val DEFAULT_LENS_FACING = CameraSelector.LENS_FACING_FRONT
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
