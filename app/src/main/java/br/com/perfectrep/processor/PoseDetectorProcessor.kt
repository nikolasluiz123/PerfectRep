package br.com.perfectrep.processor

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

/**
 * Classe responsável por encapsular o uso do [PoseDetector]
 *
 * @param options Configurações do [PoseDetector].
 */
class PoseDetectorProcessor(private var options: PoseDetectorOptions? = null) {

    private var client: PoseDetector

    init {
        if (options == null) {
            options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()

        }

        client = PoseDetection.getClient(options!!)
    }

    /**
     * Função responsável por processar o [InputImage] e retornar um callback
     * com os dados da detecção tratados.
     *
     * @param inputImage Objeto que deverá ser processado.
     * @param onSuccess Callback de sucesso.
     * @param onFailure Callback de erro.
     * @param onComplete Callback executado no sucesso ou falha.
     */
    fun process(inputImage: InputImage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit, onComplete: () -> Unit) {
        client.process(inputImage)
            .addOnSuccessListener { pose ->
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

                if (leftShoulder != null && rightShoulder != null) {
                    Log.i("Left Shoulder", "landmarkType: ${leftShoulder.landmarkType}")
                    Log.i("Left Shoulder", "position3D: ${leftShoulder.position3D}")

                    Log.i("Right Shoulder", "landmarkType: ${rightShoulder.landmarkType}")
                    Log.i("Right Shoulder", "position3D: ${rightShoulder.position3D}")
                }

                val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

                if (leftElbow != null && rightElbow != null) {
                    Log.i("Left Elbow", "landmarkType: ${leftElbow.landmarkType}")
                    Log.i("Left Elbow", "position3D: ${leftElbow.position3D}")

                    Log.i("Right Elbow", "landmarkType: ${rightElbow.landmarkType}")
                    Log.i("Right Elbow", "position3D: ${rightElbow.position3D}")
                }

                val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

                if (leftWrist != null && rightWrist != null) {
                    Log.i("Left Wrist", "landmarkType: ${leftWrist.landmarkType}")
                    Log.i("Left Wrist", "position3D: ${leftWrist.position3D}")

                    Log.i("Right Wrist", "landmarkType: ${rightWrist.landmarkType}")
                    Log.i("Right Wrist", "position3D: ${rightWrist.position3D}")
                }

                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
            .addOnCompleteListener {
                onComplete()
            }
    }
}