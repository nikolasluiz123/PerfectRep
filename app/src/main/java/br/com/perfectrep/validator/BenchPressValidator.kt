package br.com.perfectrep.validator

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.sqrt

class BenchPressValidator: IExerciseValidator {

    override fun validate(landmarks: List<PoseLandmark>): List<ValidationResult> {
        val leftElbowAngle = calculateElbowAngle(
            landmarks = landmarks,
            elbowLandmark = PoseLandmark.LEFT_ELBOW,
            shoulderLandmark = PoseLandmark.LEFT_SHOULDER,
            wristLandmark = PoseLandmark.LEFT_WRIST
        )

        val rightElbowAngle = calculateElbowAngle(
            landmarks = landmarks,
            elbowLandmark = PoseLandmark.RIGHT_ELBOW,
            shoulderLandmark = PoseLandmark.RIGHT_SHOULDER,
            wristLandmark = PoseLandmark.RIGHT_WRIST
        )

        return emptyList()
    }

    private fun calculateElbowAngle(
        landmarks: List<PoseLandmark>,
        elbowLandmark: Int,
        shoulderLandmark: Int,
        wristLandmark: Int
    ): Double {
        val elbowAxis = getLandmarkAxis(landmarks = landmarks, landmarkType = elbowLandmark)
        val shoulderAxis = getLandmarkAxis(landmarks = landmarks, landmarkType = shoulderLandmark)
        val wristAxis = getLandmarkAxis(landmarks = landmarks, landmarkType = wristLandmark)

        val elbowShoulder = subtractVectors(elbowAxis, shoulderAxis)
        val elbowWrist = subtractVectors(wristAxis, elbowAxis)

        val dotProduct = dotProduct(elbowShoulder, elbowWrist)

        val magnitudeElbowShoulder = vectorMagnitude(elbowShoulder)
        val magnitudeElbowWrist = vectorMagnitude(elbowWrist)

        val angleRadians = acos(dotProduct / (magnitudeElbowShoulder * magnitudeElbowWrist))

        return Math.toDegrees(angleRadians)
    }

    private fun getLandmarkAxis(landmarks: List<PoseLandmark>, landmarkType: Int): DoubleArray {
        val position = landmarks.find { it.landmarkType == landmarkType }!!.position3D
        return doubleArrayOf(position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
    }

    private fun subtractVectors(a: DoubleArray, b: DoubleArray): DoubleArray {
        return DoubleArray(a.size) { index -> a[index] - b[index] }
    }

    private fun dotProduct(a: DoubleArray, b: DoubleArray): Double {
        return (a.indices).sumOf { index -> a[index] * b[index] }
    }

    private fun vectorMagnitude(vector: DoubleArray): Double {
        return sqrt(vector.sumOf { it * it })
    }
}