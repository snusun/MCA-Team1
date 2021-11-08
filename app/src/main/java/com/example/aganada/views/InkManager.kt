package com.example.aganada.views

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.Ink

class InkManager {
    companion object {
        const val TAG = "InkManager"
        const val TIMEOUT_TRIGGER = 1
        const val CONVERSION_TIMEOUT_MS = 1000L
    }

    interface OnResultListener {
        fun onSuccessListener(result: String)
        fun onFailureListener()
    }

    /** Interface to register to be notified of changes in the recognized content.  */
    interface ContentChangedListener {
        /** This method is called when the recognized content changes.  */
        fun onContentChanged()
    }

    /** Interface to register to be notified of changes in the status.  */
    interface StatusChangedListener {
        /** This method is called when the recognized content changes.  */
        fun onStatusChanged()
    }

    /** Interface to register to be notified of changes in the downloaded model state.  */
    interface DownloadedModelsChangedListener {
        /** This method is called when the downloaded models changes.  */
        fun onDownloadedModelsChanged(downloadedLanguageTags: Set<String?>?)
    }

    private lateinit var recognitionTask: RecognitionTask
    private val modelManager = ModelManager()
    private val content: ArrayList<RecognitionTask.RecognizedInk> = ArrayList()
    private var strokeBuilder = Ink.Stroke.builder()
    private var inkBuilder = Ink.builder()
    private var stateChangedSinceLastRequest = false

    private var onResultListener: OnResultListener? = null
    private var contentChangedListener: ContentChangedListener? = null
    private var statusChangedListener: StatusChangedListener? = null
    private var downloadedModelsChangedListener: DownloadedModelsChangedListener? = null

    private var triggerRecognitionAfterInput = true
    private var clearCurrentInkAfterRecognition = true
    private var status = ""

    fun setTriggerRecognitionAfterInput(shouldTrigger: Boolean) {
        triggerRecognitionAfterInput = shouldTrigger
    }

    fun setClearCurrentInkAfterRecognition(shouldClear: Boolean) {
        clearCurrentInkAfterRecognition = shouldClear
    }


    // Handler to handle the UI Timeout.
    // This handler is only used to trigger the UI timeout. Each time a UI interaction happens,
    // the timer is reset by clearing the queue on this handler and sending a new delayed message (in
    // addNewTouchEvent).
    private val uiHandler = Handler(Looper.getMainLooper()) label@{ msg: Message ->
        if (msg.what == TIMEOUT_TRIGGER) {
            Log.i(TAG, "Handling timeout trigger.")
            commitResult()
            return@label true
        }
        false
    }

    private fun setStatus(newStatus: String) {
        Log.i(TAG, "Status: $newStatus")
        status = newStatus
        statusChangedListener?.onStatusChanged()
    }

    private fun commitResult() {
        val result = recognitionTask.result()
        if (recognitionTask.done() && result != null) {
            content.add(result)
            setStatus("Successful recognition: " + result.text)
            if (clearCurrentInkAfterRecognition) {
                resetCurrentInk()
            }
            if (contentChangedListener != null) {
                contentChangedListener!!.onContentChanged()
            }
            onResultListener?.onSuccessListener(result.text)
        } else {
            onResultListener?.onFailureListener()
        }
    }

    fun reset() {
        Log.i(InkManager.TAG, "reset")
        resetCurrentInk()
        content.clear()
        if (!recognitionTask.done()) {
            recognitionTask.cancel()
        }
        setStatus("")
    }

    private fun resetCurrentInk() {
        inkBuilder = Ink.builder()
        strokeBuilder = Ink.Stroke.builder()
        stateChangedSinceLastRequest = false
    }

    fun getCurrentInk(): Ink {
        return inkBuilder.build()
    }

    /**
     * This method is called when a new touch event happens on the drawing client and notifies the
     * StrokeManager of new content being added.
     *
     *
     * This method takes care of triggering the UI timeout and scheduling recognitions on the
     * background thread.
     *
     * @return whether the touch event was handled.
     */
    fun addNewTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()

        // A new event happened -> clear all pending timeout messages.
        uiHandler.removeMessages(InkManager.TIMEOUT_TRIGGER)
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(
                Ink.Point.create(x, y, t)
            )
            MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                inkBuilder.addStroke(strokeBuilder.build())
                strokeBuilder = Ink.Stroke.builder()
                stateChangedSinceLastRequest = true
                if (triggerRecognitionAfterInput) {
                    recognize()
                }
            }
            else ->         // Indicate touch event wasn't handled.
                return false
        }
        return true
    }

    // Listeners to update the drawing and status.
    fun setOnResultListener(onResultListener: OnResultListener?) {
        this.onResultListener = onResultListener
    }

    fun setContentChangedListener(contentChangedListener: ContentChangedListener?) {
        this.contentChangedListener = contentChangedListener
    }

    fun setStatusChangedListener(statusChangedListener: StatusChangedListener?) {
        this.statusChangedListener = statusChangedListener
    }

    fun setDownloadedModelsChangedListener(
        downloadedModelsChangedListener: DownloadedModelsChangedListener?
    ) {
        this.downloadedModelsChangedListener = downloadedModelsChangedListener
    }

    fun getContent(): List<RecognitionTask.RecognizedInk?>? {
        return content
    }

    fun getStatus(): String {
        return status
    }

    // Model downloading / deleting / setting.

    // Model downloading / deleting / setting.
    fun setActiveModel(languageTag: String) {
        setStatus(modelManager.setModel(languageTag))
    }

    fun deleteActiveModel(): Task<Void?> {
        return modelManager
            .deleteActiveModel()
            .addOnSuccessListener { _ -> refreshDownloadedModelsStatus() }
            .onSuccessTask { status ->
                setStatus(status)
                Tasks.forResult(null)
            }
    }

    fun download(): Task<Void?> {
        setStatus("Download started.")
        return modelManager
            .download()
            .addOnSuccessListener { _ -> refreshDownloadedModelsStatus() }
            .onSuccessTask { status ->
                setStatus(status)
                Tasks.forResult(null)
            }
    }

    // Recognition-related.

    // Recognition-related.
    fun recognize(): Task<String?> {
        if (!stateChangedSinceLastRequest || inkBuilder.isEmpty) {
            setStatus("No recognition, ink unchanged or empty")
            return Tasks.forResult(null)
        }
        return recognize(this.inkBuilder)
    }

    // Recognition-related.
    fun recognize(inkBuilder: Ink.Builder): Task<String?> {
        if (inkBuilder.isEmpty) {
            setStatus("No recognition, ink empty")
            return Tasks.forResult(null)
        }
        if (modelManager.recognizer == null) {
            setStatus("Recognizer not set")
            return Tasks.forResult(null)
        }
        return modelManager
            .checkIsModelDownloaded()
            .onSuccessTask { result ->
                if (!result) {
                    setStatus("Model not downloaded yet")
                    return@onSuccessTask Tasks.forResult(null)
                }
                stateChangedSinceLastRequest = false
                recognitionTask = RecognitionTask(modelManager.recognizer, inkBuilder.build())
                uiHandler.sendMessageDelayed(
                    uiHandler.obtainMessage(TIMEOUT_TRIGGER),
                    CONVERSION_TIMEOUT_MS
                )
                recognitionTask.run()
            }
    }

    fun refreshDownloadedModelsStatus() {
        modelManager
            .downloadedModelLanguages
            .addOnSuccessListener { downloadedLanguageTags ->
                downloadedModelsChangedListener?.onDownloadedModelsChanged(downloadedLanguageTags)
            }
    }

}
