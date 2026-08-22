package com.teamchromium.smritiai.recognition

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

object FaceDetectorHelper {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        FaceDetection.getClient(options)
    }

    @ExperimentalGetImage
    suspend fun detectFaces(imageProxy: ImageProxy): List<Face> {
        val mediaImage = imageProxy.image ?: return emptyList()

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        return try {
            detector.process(inputImage).await()
        } catch (e: Exception) {
            emptyList()
        }
    }
}