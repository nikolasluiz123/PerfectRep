package br.com.perfectrep.processor.pose

import android.util.Log
import com.google.mlkit.vision.common.PointF3D

class PoseSample(
    private val name: String,
    val className: String,
    val embedding: List<PointF3D>
) {

    companion object {
        private const val TAG = "PoseSample"

        private const val NUM_LANDMARKS = 33
        private const val NUM_DIMS = 3

        fun getPoseSample(csvLine: String, separator: String = ","): PoseSample? {
            val tokens = csvLine.split(separator).toList()
            val expectedTokenNumber = (NUM_LANDMARKS * NUM_DIMS) + 2

            if (tokens.size != expectedTokenNumber) {
                Log.e(TAG, "Invalid number of tokens for PoseSample")
                return null
            }

            val name = tokens[0]
            val className = tokens[1]
            val landmarks = List(tokens.size) { i ->
                try {
                    PointF3D.from(tokens[i].toFloat(), tokens[i + 1].toFloat(), tokens[i + 2].toFloat())
                } catch (e: Exception) {
                    when(e) {
                        is NullPointerException, is NumberFormatException -> {
                            Log.e(TAG, "Invalid value " + tokens[i] + " for landmark position.")
                            return null
                        }
                        else -> throw e
                    }
                }
            }

            return PoseSample(name, className, landmarks)
        }
    }
}