package app.teknikcell.facerecognition.detection

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceContourDetectionProcessor(
    listener: DetectionListener<List<Face>>
) : BaseImageAnalyzer<List<Face>>(listener) {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()
    )

    override fun detectInImage(image: InputImage): Task<List<Face>> = detector.process(image)

    override fun close() = detector.close()
}
