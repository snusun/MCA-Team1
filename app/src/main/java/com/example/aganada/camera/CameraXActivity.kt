package com.example.aganada.camera
/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.aganada.MainActivity
import com.example.aganada.R
import com.example.aganada.camera.objectDetector.ObjectDetectorProcessor
import com.example.aganada.camera.utils.GraphicOverlay
import com.example.aganada.camera.utils.PreferenceUtils
import com.example.aganada.camera.utils.VisionImageProcessor
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.LocalModel
import kotlinx.android.synthetic.main.activity_vision_camerax_live_preview.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.graphics.Bitmap
import android.view.*

/** Live preview demo app for ML Kit APIs using CameraX. */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
class CameraXActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    OnItemSelectedListener,
    CompoundButton.OnCheckedChangeListener {

    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var captureUseCase: ImageCapture? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var objectDetectorProcessor: ObjectDetectorProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var selectedModel = OBJECT_DETECTION_CUSTOM
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null
    private lateinit var outputDirectory: File
    private var capture = false
    private var captureTouchCoords: Pair<Int, Int>? = null
    private var captureEventType: Int? = null
    private var ko_labels: JSONObject? = null

    @RequiresApi(VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log.d(TAG, "onCreate")
        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, OBJECT_DETECTION_CUSTOM)
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        setContentView(R.layout.activity_vision_camerax_live_preview)
        previewView = findViewById(R.id.preview_view)
        if (previewView == null) {
            // Log.d(TAG, "previewView is null")
        }
        graphicOverlay = findViewById(R.id.graphic_overlay)
        if (graphicOverlay == null) {
            // Log.d(TAG, "graphicOverlay is null")
        }
        preview_view.setOnTouchListener { _, motionEvent -> takePhoto(motionEvent) }
        outputDirectory = getOutputDirectory()
        ko_labels = getKoLabels()

        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val rot : Int = when (windowManager.defaultDisplay.rotation) {
                    Surface.ROTATION_90 -> {
                        when (orientation) {
                            in 46..134 -> Surface.ROTATION_270
                            else -> Surface.ROTATION_90
                        }
                    }
                    else -> {
                        when (orientation) {
                            in 226..314 -> Surface.ROTATION_90
                            else -> Surface.ROTATION_270
                        }
                    }
                }
                if (captureUseCase != null && analysisUseCase != null){
                    captureUseCase!!.targetRotation = rot
                    analysisUseCase!!.targetRotation = rot
                    // Log.d("ROTATION", rot.toString())
                }
            }
        }
        orientationEventListener.enable()


        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(CameraXViewModel::class.java)
            .processCameraProvider
            ?.observe(
                this,
                Observer { provider: ProcessCameraProvider? ->
                    cameraProvider = provider
                    if (allPermissionsGranted()) {
                        bindAllCameraUseCases()
                    }
                }
            )

        if (!allPermissionsGranted()) {
            runtimePermissions
        }
    }

    private fun getKoLabels(): JSONObject?{
        var jsonString: String
        try{
            jsonString = assets.open("ko_labels.json").bufferedReader().use { it.readText() }
        } catch (e: IOException){
            e.printStackTrace()
            return null
        }
        return JSONObject(jsonString)
    }

    @RequiresApi(VERSION_CODES.N)
    private fun getOutputDirectory(): File{
        var dir = File(dataDir.canonicalPath+File.separator+"files/tmp")
        if(!dir.exists()) {
            dir.mkdir()
            // Log.d("OUTPUTDIR", "make directory")
        }
        // Log.d("OUTPUTDIR", dir.toString())
        return dir
    }

    private fun takePhoto(event: MotionEvent): Boolean{
        // Log.d(TAG, "takePhoto - ${event.action}")
        return when(event.action){
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_MOVE -> true
            MotionEvent.ACTION_UP -> {
                captureTouchCoords = Pair(event.x.toInt(), event.y.toInt())
                capture = true
                captureEventType = event.action
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(STATE_SELECTED_MODEL, selectedModel)
    }

    @Synchronized
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent?.getItemAtPosition(pos).toString()
        // Log.d(TAG, "Selected model: $selectedModel")
        bindAnalysisUseCase()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing.
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (cameraProvider == null) {
            return
        }
        val newLensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
        val newCameraSelector = CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                // Log.d(TAG, "Set facing to " + newLensFacing)
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                return
            }
        } catch (e: CameraInfoUnavailableException) {
            // Falls through
        }
        Toast.makeText(
            applicationContext,
            "본 디바이스에는 정방향 렌즈가 존재하지 않습니다.",
            Toast.LENGTH_SHORT
        )
            .show()
    }

    public override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
        // Hide the action bar.
        supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()

        imageProcessor?.run { this.stop() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }

    private fun bindAllCameraUseCases() {
        // Log.d(TAG, "bindAllCameraUseCases")
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
            bindCaptureUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }

        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView!!.getSurfaceProvider())
        cameraProvider!!.bindToLifecycle( this, cameraSelector!!, previewUseCase)
    }
    private fun bindCaptureUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (captureUseCase != null) {
            cameraProvider!!.unbind(captureUseCase)
        }

        val builder = ImageCapture.Builder()

        captureUseCase = builder.build()
        cameraProvider!!.bindToLifecycle( this, cameraSelector!!, captureUseCase)
    }


    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }

        imageProcessor =
            try {
                when (selectedModel) {
                    OBJECT_DETECTION_CUSTOM -> {
                        Log.i(TAG, "Using Custom Object Detector (with object labeler) Processor")
                        val localModel =
                            LocalModel.Builder().setAssetFilePath("custom_models/object_labeler.tflite").build()
                        val customObjectDetectorOptions =
                            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel)
                        objectDetectorProcessor = customObjectDetectorOptions?.let { ObjectDetectorProcessor(this, it) }
                        objectDetectorProcessor
                    }
                    else -> throw IllegalStateException("Invalid model name")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Can not create image processor: $selectedModel", e)
                Toast.makeText(
                    applicationContext,
                    "이미지 프로세서를 구현하지 못 했습니다: " + e.localizedMessage,
                    Toast.LENGTH_LONG
                )
                    .show()
                return
            }

        val builder = ImageAnalysis.Builder()
        val targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        builder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay!!.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                    } else {
                        graphicOverlay!!.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    // !! -> ?
                    imageProcessor?.processImageProxy(imageProxy, graphicOverlay)
                    if(capture){
                        capture = false
                        // Log.d("HYUNSOO", "capture is on -> save image -$capture")
                        capturePhoto(imageProxy)
                    }

                } catch (e: MlKitException) {
                    Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
        cameraProvider!!.bindToLifecycle(this, cameraSelector!!, analysisUseCase)
    }

    private fun getDetectionInfo(image: ImageProxy): Pair<Rect?, String?>{
        // Log.d("HYUNSOO", "saveImage -$capture")
        if(objectDetectorProcessor != null){
            // Log.d(TAG, "ObjectDetectorProcessor properly instantiated")
            // Case 1: no detected object in current image
            if(objectDetectorProcessor?.getDetectedObjects().isNullOrEmpty()){
                Log.e(TAG, "Tried to save the image of a non-detected object")
                Toast.makeText(baseContext, "인식하지 못 했습니다. 다시 찍어주세요!",
                    Toast.LENGTH_SHORT).show()
                return Pair(null, null)
            }
            // Case 2: there are detected objects in current image
            val detectedObjects = objectDetectorProcessor?.getDetectedObjects()
            if (detectedObjects != null) {
                var targetLabel = ""
                var targetBoundingBox: Rect? = null
                var targetBoundingBoxF: RectF? = null

                // Get detection info for the touched image
                for (detectedObject in detectedObjects) {
                    targetBoundingBoxF = convertCoordinates(detectedObject.boundingBox, image)
                    // Log.d("HYUNSOO", "F coordinates - " + targetBoundingBoxF!!.left + " " +
                    //        targetBoundingBoxF.top + " " + targetBoundingBoxF.right + " " +
                    //        targetBoundingBoxF.bottom)
                    if (targetBoundingBoxF.contains(
                            captureTouchCoords!!.first.toFloat(),
                            captureTouchCoords!!.second.toFloat()
                        )
                    ) {
                        targetBoundingBox = detectedObject.boundingBox
                        // select label with highest confidence
                        val confidence = 0
                        for (label in detectedObject.labels){
                            if(label.confidence > confidence){
                                targetLabel = label.text
                            }
                        }
                        // Log.d("HYUNSOO", "touched inside box")
                        break
                    }
                }
                // Case 2-1: touched inside one of the detected objects' box
                if(targetBoundingBox != null){
                    targetBoundingBox = maintainRatio(targetBoundingBox, image.width, image.height)
                    return Pair(targetBoundingBox, targetLabel)
                }
                // Case 2-2: touched outside all detected objects' boxes
                else{
                    // Log.d("HYUNSOO", "targetboundingbox is null... touched outside a valid box" +
                    //        captureTouchCoords.toString() + ", " + targetBoundingBox.toString()
                    )
                    Toast.makeText(baseContext, "네모칸 안쪽을 터치해주세요!.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Log.e(TAG, "ObjectDetectorProcessor was not properly instantiated")
            Toast.makeText(baseContext, "ObjectDetector가 제대로 시작되지 못 했습니다.", Toast.LENGTH_SHORT).show()
        }
        return Pair(null, null)
    }

    private fun capturePhoto(image: ImageProxy){
        val imageCapture = captureUseCase ?: return
        // Save temporary image file using CameraX capture
        val tempFile = File(
            outputDirectory, "temp.jpeg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("11-19", "Photo capture failed: ${exc.message}", exc)
                }

                @RequiresApi(VERSION_CODES.P)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cropAndSaveImage(image)
                }
            })
    }

    @RequiresApi(VERSION_CODES.P)
    private fun cropAndSaveImage(image: ImageProxy){
        // Get detected object's info
        var (targetBoundingBox, targetLabel) = getDetectionInfo(image)!!
        if(targetBoundingBox == null) return
        var finalLabel = targetLabel
        try {
            finalLabel = ko_labels!!.get(targetLabel) as String
            // Log.d("TRANSLATION", "$finalLabel")
        } catch (e: JSONException){
            e.printStackTrace()
        }

        // Create final output file stream
        val photoFile = File(
            outputDirectory,
            finalLabel + "_" + SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpeg")

        // Get image's bitmap and crop with adjustment
        val tempFile = File(
            outputDirectory, "temp.jpeg");
        val source = ImageDecoder.createSource(tempFile);
        val bitmap = ImageDecoder.decodeBitmap(source);
        // Log.d("11-19", "targetBoundingBox before: ${targetBoundingBox.left}, ${targetBoundingBox.right}," +
        //        "${targetBoundingBox.top}, ${targetBoundingBox.bottom}")
        targetBoundingBox = Rect(targetBoundingBox.left * bitmap.width / image.width,
        targetBoundingBox.top * bitmap.height / image.height,
            targetBoundingBox.right * bitmap.width / image.width,
            targetBoundingBox.bottom * bitmap.height / image.height)
        // Log.d("11-19", "image: ${image.width}, ${image.height}")
        // Log.d("11-19", "bitmap: ${bitmap.width}, ${bitmap.height}")
        // Log.d("11-19", "targetBoundingBox after: ${targetBoundingBox.left}, ${targetBoundingBox.right}," +
        //        "${targetBoundingBox.top}, ${targetBoundingBox.bottom}")
        val croppedBitmap = Bitmap.createBitmap(bitmap,
            targetBoundingBox.left, targetBoundingBox.top,
            targetBoundingBox.width(), targetBoundingBox.height())

        // Log.d("11-19", "image: ${image.width}, ${image.height}")
        // Log.d("11-19", "targetBoundingBox: ${targetBoundingBox.left}, ${targetBoundingBox.right}," +
        //        "${targetBoundingBox.top}, ${targetBoundingBox.bottom}")
        // Log.d("11-19", "bitmap: ${bitmap.width}, ${bitmap.height}")
        // Log.d("11-19", "croppedBitmap: ${croppedBitmap.width}, ${croppedBitmap.height}")

        // Save image to file
        try {
            val stream: OutputStream = FileOutputStream(photoFile)

            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
            // Log.d("11-19", "Successfully saved image at ${photoFile.absoluteFile}")
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("captured_image_name", Uri.parse(photoFile.absolutePath).toString())
            }
            startActivity(intent)

        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
    }

    /*
        Width must always be 4, height is 3
     */
    private fun maintainRatio(box: Rect, bitmapWidth: Int, bitmapHeight: Int): Rect{
        // Log.d("RATIO", box.flattenToString())
        var newWidth: Float;
        var newHeight: Float;
        // find the larger edge and fix it to calculate the other in-ratio edge length
        if ( box.width() > box.height() ){
            newWidth = box.width().toFloat()
            newHeight = (newWidth * 3 / 4 + 1)
            // Log.d("RATIO", "${box.width()} -> new height: ${(newWidth * (3/4) + 1).toFloat()} vs $newHeight")
        }
        else{
            newHeight = box.height().toFloat()
            newWidth = (newHeight * 4 / 3 + 1)
            // Log.d("RATIO", "${box.height()} -> new width: ${(newHeight * (4/3) + 1).toFloat()} vs $newWidth")
        }
        // center the box
        var dx = ((newWidth - box.width()) / 2).toInt()
        var dy = ((newHeight - box.height()) / 2).toInt()
        box.set(box.left-dx, box.top-dy, box.right+dx, box.bottom+dy)

        // Log.d("RATIO", "newBox $dx, $dy -> ${box.flattenToString()}")

        // Handle illegal argument exception
        if (box.width() > bitmapWidth || box.height() > bitmapHeight){
            box.set(0, 0, bitmapWidth, bitmapHeight)
        }
        if (box.left < 0 ){
            dx = abs(box.left)
            box.set(0, box.top, box.right + dx, box.bottom)
        }
        if (box.top < 0){
            dy = abs(box.top)
            box.set(box.left, 0, box.right, box.bottom + dy)
        }
        if (box.right > bitmapWidth) {
            dx = box.right - bitmapWidth
            box.set(box.left - dx, box.top, bitmapWidth, box.bottom)
        }
        if (box.bottom > bitmapHeight) {
            dy = box.bottom - bitmapHeight
            box.set(box.left, box.top-dy, box.right, bitmapHeight)
        }
        // Log.d("RATIO", "return valid coordinates ${box.flattenToString()}, $bitmapHeight, $bitmapWidth")
        return box
    }

    // mock of GraphicOverlay's translateX and translateY
    private fun translateXY(x: Float, y: Float, imageHeight: Float, imageWidth: Float): Pair<Float, Float>{
        val width = (previewView!!.width).toFloat()
        val height = (previewView!!.height).toFloat()
        val viewAspectRatio: Float = (width / height).toFloat()
        val imageAspectRatio: Float = (imageWidth / imageHeight).toFloat()
        var postScaleWidthOffset = 0F
        var postScaleHeightOffset = 0F
        var scaleFactor = 1.0F
        if (viewAspectRatio > imageAspectRatio) {
            // Log.d("HYUNSOO", "height is scaled")
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = (width / imageWidth).toFloat()
            postScaleHeightOffset = (width / imageAspectRatio - height) / 2
        } else {
            // Log.d("HYUNSOO", "width is scaled")
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = (height / imageHeight).toFloat()
            postScaleWidthOffset = (height * imageAspectRatio - width) / 2
        }
        // Log.d("HYUNSOO", "image ($imageHeight, $imageWidth) / view ($height, $width) " +
        //        "/ ratios ($imageAspectRatio, $viewAspectRatio) / " +
        //        "scaleH ($postScaleHeightOffset, $postScaleWidthOffset, $scaleFactor) /")
        return Pair(abs(x*scaleFactor - postScaleWidthOffset), abs(y*scaleFactor -  postScaleHeightOffset))
    }

    // convert x, y from image coordinates to view coordinates
    private fun convertCoordinates(boundingBox: Rect, image: ImageProxy): RectF{
        val rect = RectF(boundingBox)
        val xy0 = translateXY(rect.left, rect.top, (image.height).toFloat(), (image.width).toFloat())
        val xy1 = translateXY(rect.right, rect.bottom, (image.height).toFloat(), (image.width).toFloat())
        rect.left = min(xy0.first, xy1.first)
        rect.right = max(xy0.first, xy1.first)
        rect.top = xy0.second
        rect.bottom = xy1.second
        return rect
    }

    private val requiredPermissions: Array<String?>
        get() =
            try {
                val info =
                    this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            bindAllCameraUseCases()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val TAG = "CameraXLivePreview"
        private const val PERMISSION_REQUESTS = 1
        private const val OBJECT_DETECTION = "Object Detection"
        private const val OBJECT_DETECTION_CUSTOM = "Custom Object Detection"
        private const val STATE_SELECTED_MODEL = "selected_model"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private fun isPermissionGranted(context: Context, permission: String?): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}
