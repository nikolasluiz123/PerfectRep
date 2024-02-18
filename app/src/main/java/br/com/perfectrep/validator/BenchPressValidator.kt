package br.com.perfectrep.validator

import android.util.Log
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.tan

class BenchPressValidator : IExerciseValidator {

    override fun validate(landmarks: List<PoseLandmark>): List<ValidationResult> {
        val left = landmarks.filter {
            it.landmarkType == PoseLandmark.LEFT_SHOULDER ||
                    it.landmarkType == PoseLandmark.LEFT_ELBOW ||
                    it.landmarkType == PoseLandmark.LEFT_WRIST
        }

        val right = landmarks.filter {
            it.landmarkType == PoseLandmark.RIGHT_SHOULDER ||
                    it.landmarkType == PoseLandmark.RIGHT_ELBOW ||
                    it.landmarkType == PoseLandmark.RIGHT_WRIST
        }

        Log.i("Teste", "Ângulo do Cotovelo Esquerdo: ${getAngle(left[0], left[1], left[2])}")
        Log.i("Teste", "Ângulo do Cotovelo Direito: ${getAngle(right[0], right[1], right[2])}")

        return emptyList()
    }

    private fun getAngle(firstPoint: PoseLandmark, midPoint: PoseLandmark, lastPoint: PoseLandmark): Double {
        var result = Math.toDegrees(
            (atan2(
                lastPoint.position.y - midPoint.position.y,
                lastPoint.position.x - midPoint.position.x
            ) - atan2(
                firstPoint.position.y - midPoint.position.y,
                firstPoint.position.x - midPoint.position.x
            )).toDouble()
        )
        result = abs(result) // Angle should never be negative

        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }

        return result
    }
}