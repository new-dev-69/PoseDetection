//package com.example.pose_detection

////import android.R
////import android.content.pm.PackageManager
////import android.os.Bundle
////import androidx.activity.ComponentActivity
////import androidx.activity.compose.setContent
////import androidx.activity.enableEdgeToEdge
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.padding
////import androidx.compose.material3.Scaffold
////import androidx.compose.material3.Text
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.tooling.preview.Preview
////import com.example.pose_detection.ui.theme.Pose_DetectionTheme
////import androidx.core.app.ActivityCompat.requestPermissions
////import androidx.core.content.PermissionChecker.checkSelfPermission
//
////class MainActivity : ComponentActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContent {
////            Pose_DetectionTheme {
////                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
////                    Greeting(
////                        name = "Android",
////                        modifier = Modifier.padding(innerPadding)
////                    )
////                }
////            }
////        }
////        get_permissions()
////    }
////}
//
////fun get_permissions() {
////    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
////        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
////    }
////}
////
////override fun onRequestPermissionsResult(
////    requestCode: Int,
////    permissions: Array<out String>,
////    grantResults: IntArray
////) {
////    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////
////    if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
////}
////
////@Composable
////fun Greeting(name: String, modifier: Modifier = Modifier) {
////    Text(
////        text = "Hello $name!",
////        modifier = modifier
////    )
////}
////
////@Preview(showBackground = true)
////@Composable
////fun GreetingPreview() {
////    Pose_DetectionTheme {
////        Greeting("Android")
////    }
////}
//
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.*
//import android.hardware.camera2.CameraCaptureSession
//import android.hardware.camera2.CameraDevice
//import android.hardware.camera2.CameraManager
////import android.hardware.camera2.CaptureRequest
////import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.Handler
//import android.os.HandlerThread
//import android.util.Log
//import android.view.Surface
//import android.view.TextureView
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//import com.example.pose_detection.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
//import org.tensorflow.lite.DataType
//import org.tensorflow.lite.support.image.ImageProcessor
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.support.image.ops.ResizeOp
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//
//class MainActivity : AppCompatActivity() {
//
//    val paint = Paint()
//    lateinit var imageProcessor: ImageProcessor
//    lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
//    lateinit var bitmap: Bitmap
//    lateinit var imageView: ImageView
//    lateinit var handler:Handler
//    lateinit var handlerThread: HandlerThread
//    lateinit var textureView: TextureView
//    lateinit var cameraManager: CameraManager
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        get_permissions()
//
//        imageProcessor = ImageProcessor.Builder().add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR)).build()
//        model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this)
//        imageView = findViewById(R.id.imageView)
//        textureView = findViewById(R.id.textureView)
//        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        handlerThread = HandlerThread("videoThread")
//        handlerThread.start()
//        handler = Handler(handlerThread.looper)
//
//        paint.setColor(Color.YELLOW)
//
//        textureView.surfaceTextureListener = object:TextureView.SurfaceTextureListener{
//            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
//                open_camera()
//            }
//
//            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
//
//            }
//
//            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
//                return false
//            }
//
//            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
//                bitmap = textureView.bitmap!!
//                var tensorImage = TensorImage(DataType.UINT8)
//                tensorImage.load(bitmap)
//                tensorImage = imageProcessor.process(tensorImage)
//
//                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.UINT8)
//                inputFeature0.loadBuffer(tensorImage.buffer)
//
//                val outputs = model.process(inputFeature0)
//                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
//
//                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//                var canvas = Canvas(mutable)
//                var h = bitmap.height
//                var w = bitmap.width
//                var x = 0
//
//                Log.d("output__", outputFeature0.size.toString())
//                while(x <= 49){
//                    if(outputFeature0.get(x+2) > 0.45){
//                        canvas.drawCircle(outputFeature0.get(x+1)*w, outputFeature0.get(x)*h, 10f, paint)
//                    }
//                    x+=3
//                }
//
//                imageView.setImageBitmap(mutable)
//            }
//        }
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        model.close()
//    }
//
//    @SuppressLint("MissingPermission")
//    fun open_camera(){
//        cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
//            override fun onOpened(p0: CameraDevice) {
//                var captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//                var surface = Surface(textureView.surfaceTexture)
//                captureRequest.addTarget(surface)
//                p0.createCaptureSession(listOf(surface), object:CameraCaptureSession.StateCallback(){
//                    override fun onConfigured(p0: CameraCaptureSession) {
//                        p0.setRepeatingRequest(captureRequest.build(), null, null)
//                    }
//                    override fun onConfigureFailed(p0: CameraCaptureSession) {
//
//                    }
//                }, handler)
//            }
//
//            override fun onDisconnected(p0: CameraDevice) {
//
//            }
//
//            override fun onError(p0: CameraDevice, p1: Int) {
//
//            }
//        }, handler)
//    }
//
//    fun get_permissions(){
//        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
//        }
//    }
//    override fun onRequestPermissionsResult(  requestCode: Int, permissions: Array<out String>, grantResults: IntArray  ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if(grantResults[0] != PackageManager.PERMISSION_GRANTED) get_permissions()
//    }
//}


package com.example.pose_detection

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.pose_detection.ml.LiteModelMovenetSingleposeLightningTfliteFloat164
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

    private val paint = Paint().apply { color = Color.YELLOW }

    private lateinit var imageProcessor: ImageProcessor
    private lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
    private lateinit var imageView: ImageView
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        model = LiteModelMovenetSingleposeLightningTfliteFloat164.newInstance(this)

        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        handlerThread = HandlerThread("videoThread").apply { start() }
        handler = Handler(handlerThread.looper)

        // Check camera permission
        getPermissions()
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//            val bitmap = textureView.bitmap ?: return
//
//            var tensorImage = TensorImage(DataType.UINT8)
//            tensorImage.load(bitmap)
//            tensorImage = imageProcessor.process(tensorImage)
//
//            val inputFeature0 = TensorBuffer.createFixedSize(
//                intArrayOf(1, 192, 192, 3),
//                DataType.UINT8
//            )
//            inputFeature0.loadBuffer(tensorImage.buffer)
//
//            val outputs = model.process(inputFeature0)
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
//
//            val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//            val canvas = Canvas(mutable)
//            val h = bitmap.height
//            val w = bitmap.width
//
//            var x = 0
//            while (x + 2 < outputFeature0.size) {
//                if (outputFeature0[x + 2] > 0.45) {
//                    canvas.drawCircle(
//                        outputFeature0[x + 1] * w,
//                        outputFeature0[x] * h,
//                        10f,
//                        paint
//                    )
//                }
//                x += 3
//            }
//
//            imageView.setImageBitmap(mutable)
//            val bitmap = textureView.bitmap ?: return
//
//            var tensorImage = TensorImage(DataType.UINT8)
//            tensorImage.load(bitmap)
//            tensorImage = imageProcessor.process(tensorImage)
//
//            val inputFeature0 = TensorBuffer.createFixedSize(
//                intArrayOf(1, 192, 192, 3),
//                DataType.UINT8
//            )
//            inputFeature0.loadBuffer(tensorImage.buffer)
//
//            val outputs = model.process(inputFeature0)
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
//
//            val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//            val canvas = Canvas(mutable)
//            val h = bitmap.height
//            val w = bitmap.width
//
//            // Draw keypoints
//            val keypoints = mutableListOf<Triple<Float, Float, Float>>() // (x, y, confidence)
//            var x = 0
//            while (x + 2 < outputFeature0.size) {
//                val yNorm = outputFeature0[x]
//                val xNorm = outputFeature0[x + 1]
//                val conf = outputFeature0[x + 2]
//
//                if (conf > 0.45) {
//                    val px = xNorm * w
//                    val py = yNorm * h
//                    keypoints.add(Triple(px, py, conf))
//                    canvas.drawCircle(px, py, 10f, paint)
//                }
//                x += 3
//            }
//
//            // ---- Pose Classification Example ----
//            var detectedPose = "Unknown"
//            var confidence = 0f
//
//            if (keypoints.size >= 11) { // Make sure we have enough keypoints
//                // Example indices (MoveNet order: [y, x, score] per landmark)
//                val leftShoulder = keypoints.getOrNull(5)
//                val rightShoulder = keypoints.getOrNull(6)
//                val leftWrist = keypoints.getOrNull(9)
//                val rightWrist = keypoints.getOrNull(10)
//
//                if (leftShoulder != null && rightShoulder != null &&
//                    leftWrist != null && rightWrist != null) {
//
//                    if (leftWrist.second < leftShoulder.second &&
//                        rightWrist.second < rightShoulder.second) {
//                        detectedPose = "Hands Up"
//                        confidence = (leftWrist.third + rightWrist.third) / 2
//                    }
//                }
//            }
//
//            // Draw text on screen
//            val textPaint = Paint().apply {
//                color = Color.RED
//                textSize = 60f
//                style = Paint.Style.FILL
//            }
//            canvas.drawText("$detectedPose (${(confidence * 100).toInt()}%)", 50f, 100f, textPaint)
//
//            imageView.setImageBitmap(mutable)
            val bitmap = textureView.bitmap ?: return

            var tensorImage = TensorImage(DataType.UINT8)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            val inputFeature0 = TensorBuffer.createFixedSize(
                intArrayOf(1, 192, 192, 3),
                DataType.UINT8
            )
            inputFeature0.loadBuffer(tensorImage.buffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutable)
            val h = bitmap.height
            val w = bitmap.width

            // Collect keypoints
            val keypoints = mutableListOf<Triple<Float, Float, Float>>() // (x, y, confidence)
            var x = 0
            while (x + 2 < outputFeature0.size) {
                val yNorm = outputFeature0[x]
                val xNorm = outputFeature0[x + 1]
                val conf = outputFeature0[x + 2]

                if (conf > 0.45) {
                    val px = xNorm * w
                    val py = yNorm * h
                    keypoints.add(Triple(px, py, conf))
                    canvas.drawCircle(px, py, 10f, paint)
                } else {
                    keypoints.add(Triple(-1f, -1f, conf)) // placeholder for missing point
                }
                x += 3
            }

            // ---- Pose Classification ----
            var detectedPose = "Unknown"
            var confidence = 0f

            if (keypoints.size >= 17) {
                val leftShoulder = keypoints[5]
                val rightShoulder = keypoints[6]
                val leftElbow = keypoints[7]
                val rightElbow = keypoints[8]
                val leftWrist = keypoints[9]
                val rightWrist = keypoints[10]
                val leftHip = keypoints[11]
                val rightHip = keypoints[12]
                val leftKnee = keypoints[13]
                val rightKnee = keypoints[14]

                // Hands Up: wrists above shoulders
                if (leftWrist.second > 0 && rightWrist.second > 0 &&
                    leftShoulder.second > 0 && rightShoulder.second > 0 &&
                    leftWrist.second < leftShoulder.second &&
                    rightWrist.second < rightShoulder.second
                ) {
                    detectedPose = "Hands Up"
                    confidence = (leftWrist.third + rightWrist.third) / 2
                }
                // T-Pose: arms horizontal
                else if (leftWrist.second > 0 && rightWrist.second > 0 &&
                    Math.abs(leftWrist.second - leftShoulder.second) < 50 &&
                    Math.abs(rightWrist.second - rightShoulder.second) < 50
                ) {
                    detectedPose = "T-Pose"
                    confidence = (leftWrist.third + rightWrist.third) / 2
                }
                // Squat: hips below knees
                else if (leftHip.second > 0 && rightHip.second > 0 &&
                    leftKnee.second > 0 && rightKnee.second > 0 &&
                    leftHip.second > leftKnee.second &&
                    rightHip.second > rightKnee.second
                ) {
                    detectedPose = "Squat"
                    confidence = (leftHip.third + rightHip.third) / 2
                }
                // Sitting: hips near knees (within small distance)
                else if (leftHip.second > 0 && rightHip.second > 0 &&
                    leftKnee.second > 0 && rightKnee.second > 0 &&
                    Math.abs(leftHip.second - leftKnee.second) < 60 &&
                    Math.abs(rightHip.second - rightKnee.second) < 60
                ) {
                    detectedPose = "Sitting"
                    confidence = (leftHip.third + rightHip.third) / 2
                }
            }

            // Draw text
            val textPaint = Paint().apply {
                color = Color.RED
                textSize = 60f
                style = Paint.Style.FILL
            }
            canvas.drawText(
                "$detectedPose (${(confidence * 100).toInt()}%)",
                50f,
                100f,
                textPaint
            )

            imageView.setImageBitmap(mutable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
        handlerThread.quitSafely()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(textureView.surfaceTexture)
                captureRequest.addTarget(surface)

                camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(captureRequest.build(), null, handler)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                }, handler)
            }

            override fun onDisconnected(camera: CameraDevice) {}
            override fun onError(camera: CameraDevice, error: Int) {}
        }, handler)
    }

    private fun getPermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        } else {
            // Permission already granted
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            textureView.surfaceTextureListener = textureListener
        }
    }
}
