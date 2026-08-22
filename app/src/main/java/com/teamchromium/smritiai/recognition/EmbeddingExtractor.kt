package com.teamchromium.smritiai.recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

object EmbeddingExtractor {

    private const val MODEL_FILE = "mobilefacenet.tflite"
    private const val INPUT_SIZE = 112          // model expects 112x112 face images
    private const val EMBEDDING_SIZE = 192      // MobileFaceNet outputs 192 numbers per face

    private var interpreter: Interpreter? = null

    private fun getInterpreter(context: Context): Interpreter {
        return interpreter ?: run {
            val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val modelBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
            Interpreter(modelBuffer).also { interpreter = it }
        }
    }

    fun extractEmbedding(context: Context, faceBitmap: Bitmap, faceBounds: Rect): FloatArray {
        val cropped = cropToFace(faceBitmap, faceBounds)
        val resized = Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = bitmapToByteBuffer(resized)

        val outputBuffer = Array(1) { FloatArray(EMBEDDING_SIZE) }
        getInterpreter(context).run(inputBuffer, outputBuffer)

        return outputBuffer[0]
    }

    private fun cropToFace(bitmap: Bitmap, bounds: Rect): Bitmap {
        val safeLeft = bounds.left.coerceIn(0, bitmap.width)
        val safeTop = bounds.top.coerceIn(0, bitmap.height)
        val safeRight = bounds.right.coerceIn(safeLeft, bitmap.width)
        val safeBottom = bounds.bottom.coerceIn(safeTop, bitmap.height)
        return Bitmap.createBitmap(
            bitmap, safeLeft, safeTop, safeRight - safeLeft, safeBottom - safeTop
        )
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        return buffer
    }
}