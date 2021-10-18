package com.example.aganada.camera.utils

import android.content.Context
import android.os.Build.VERSION_CODES
import android.preference.PreferenceManager
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import com.example.aganada.R
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions

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

/** Utility class to retrieve shared preferences.  */
object PreferenceUtils {
    @RequiresApi(VERSION_CODES.LOLLIPOP)
    fun getCameraXTargetResolution(context: Context, lensfacing: Int): Size? {
        val prefKey =
            if (lensfacing == CameraSelector.LENS_FACING_BACK) context.getString(R.string.pref_key_camerax_rear_camera_target_resolution)
            else context.getString(
                R.string.pref_key_camerax_front_camera_target_resolution
            )
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        try {
            return Size.parseSize(sharedPreferences.getString(prefKey, null))
        } catch (e: Exception) {
            return null
        }
    }

    fun shouldHideDetectionInfo(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_info_hide)
        return sharedPreferences.getBoolean(prefKey, false)
    }

    fun getCustomObjectDetectorOptionsForLivePreview(
        context: Context?, localModel: LocalModel?
    ): CustomObjectDetectorOptions? {
        return getCustomObjectDetectorOptions(
            context!!,
            localModel!!,
            R.string.pref_key_live_preview_object_detector_enable_multiple_objects,
            R.string.pref_key_live_preview_object_detector_enable_classification,
            CustomObjectDetectorOptions.STREAM_MODE
        )
    }
    
    private fun getCustomObjectDetectorOptions(
        context: Context,
        localModel: LocalModel,
        @StringRes prefKeyForMultipleObjects: Int,
        @StringRes prefKeyForClassification: Int,
        @ObjectDetectorOptionsBase.DetectorMode mode: Int
    ): CustomObjectDetectorOptions {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val enableMultipleObjects =
            sharedPreferences.getBoolean(context.getString(prefKeyForMultipleObjects), false)
        val enableClassification =
            sharedPreferences.getBoolean(context.getString(prefKeyForClassification), true)
        val builder = CustomObjectDetectorOptions.Builder(localModel).setDetectorMode(mode)
        if (enableMultipleObjects) {
            builder.enableMultipleObjects()
        }
        if (enableClassification) {
            builder.enableClassification().setMaxPerObjectLabelCount(1)
        }
        return builder.build()
    }

    fun isCameraLiveViewportEnabled(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_camera_live_viewport)
        return sharedPreferences.getBoolean(prefKey, false)
    }
}