package br.com.perfectrep.validator

import com.google.mlkit.vision.pose.PoseLandmark

interface IExerciseValidator {

    fun validate(landmarks: List<PoseLandmark>): List<ValidationResult>
}