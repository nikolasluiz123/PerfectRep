package br.com.perfectrep.processor.pose

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
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
    fun process(
        inputImage: InputImage,
        onSuccess: (Pose) -> Unit,
        onFailure: (Exception) -> Unit,
        onComplete: (Task<Pose>) -> Unit
    ) {
        client.process(inputImage)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
            .addOnCompleteListener(onComplete)
    }
}