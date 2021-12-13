package com.example.aganada.camera

import android.app.Application
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException

/** View model for interacting with CameraX.  */
class CameraXViewModel
/**
 * Create an instance which interacts with the camera service via the given application context.
 */
    (application: Application) : AndroidViewModel(application) {
    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    // Handle any errors (including cancellation) here.
    val processCameraProvider: MutableLiveData<ProcessCameraProvider>?
        get() {
            if (cameraProviderLiveData == null) {
                cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
                val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                    ProcessCameraProvider.getInstance(getApplication())
                cameraProviderFuture.addListener(
                    {
                        try {
                            cameraProviderLiveData!!.setValue(cameraProviderFuture.get())
                        } catch (e: ExecutionException) {
                            // Handle any errors (including cancellation) here.
                            Log.e(
                                TAG,
                                "Unhandled exception",
                                e
                            )
                        } catch (e: InterruptedException) {
                            Log.e(
                                TAG,
                                "Unhandled exception",
                                e
                            )
                        }
                    },
                    ContextCompat.getMainExecutor(getApplication())
                )
            }
            return cameraProviderLiveData
        }

    companion object {
        private const val TAG = "CameraXViewModel"
    }
}