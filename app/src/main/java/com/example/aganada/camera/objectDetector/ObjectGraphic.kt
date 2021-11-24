package com.example.aganada.camera.objectDetector

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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.aganada.camera.utils.GraphicOverlay
import com.google.mlkit.vision.objects.DetectedObject
import kotlin.math.max
import kotlin.math.min

/** Draw the detected object info in preview.  */
class ObjectGraphic constructor(
    overlay: GraphicOverlay,
    private val detectedObject: DetectedObject
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaints = Paint()

    init {
        boxPaints.color = Color.argb(0xFF, 0x5C, 0x9F, 0xDB)
        boxPaints.style = Paint.Style.STROKE
        boxPaints.strokeWidth = 8.0f
    }

    override fun draw(canvas: Canvas) {
        val rect = RectF(detectedObject.boundingBox)
        val x0 = translateX(rect.left)
        val x1 = translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = translateY(rect.top)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, boxPaints)
    }
}
