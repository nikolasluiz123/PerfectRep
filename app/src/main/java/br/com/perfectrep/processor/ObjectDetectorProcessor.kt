package br.com.perfectrep.processor

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

/**
 * Classe responsável por encapsular o uso do [ObjectDetector]
 *
 * @param options Configurações do [ObjectDetector].
 */
class ObjectDetectorProcessor(private var options: ObjectDetectorOptions? = null) {

    private var client: ObjectDetector

    init {
        if (options == null) {
            options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .enableMultipleObjects()
                .build()
        }

        client = ObjectDetection.getClient(options!!)
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
            .addOnSuccessListener { detectedObjects ->
                detectedObjects.forEach { detectedObject ->
                    if (detectedObject.labels.isNotEmpty()) {
                        Log.i("Detectado", "Tracking Id: ${detectedObject.trackingId}")

                        detectedObject.labels.forEach { label ->
                            Log.i("Detectado", "Label Text: ${label.text}")
                            Log.i("Detectado", "Label Index: ${label.index}")
                            Log.i("Detectado", "Label Confidence: ${label.confidence}")
                        }
                    }

                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ERRO", "Houve um erro ao processar o InputImage para detectar os objetos.", e)
                onFailure(e)
            }
            .addOnCompleteListener {
                onComplete()
            }
    }
}