package br.com.perfectrep.processor.pose

import android.util.Pair
import br.com.perfectrep.utils.LandmarkPositionUtils
import br.com.perfectrep.utils.LandmarkPositionUtils.maxAbs
import br.com.perfectrep.utils.LandmarkPositionUtils.multiply
import br.com.perfectrep.utils.LandmarkPositionUtils.sumAbs
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

class PoseClassifier(
    private val poseSamples: List<PoseSample>,
    private val maxDistanceTopK: Int = MAX_DISTANCE_TOP_K,
    private val meanDistanceTopK: Int = MEAN_DISTANCE_TOP_K,
    private val axesWeights: PointF3D = AXES_WEIGHTS
) {

    fun classify(pose: Pose): ClassificationResult {
        return classify(extractPoseLandmarks(pose))
    }

    private fun classify(landmarks: List<PointF3D>): ClassificationResult {
        val result = ClassificationResult()

        if (landmarks.isEmpty()) {
            return result
        }

        val flippedLandmarks: MutableList<PointF3D> = ArrayList(landmarks)
        LandmarkPositionUtils.multiplyAll(flippedLandmarks, PointF3D.from(-1f, 1f, 1f))

        val embedding: List<PointF3D> = PoseEmbedding.getPoseEmbedding(landmarks)
        val flippedEmbedding: List<PointF3D> = PoseEmbedding.getPoseEmbedding(flippedLandmarks)

        // Classification is done in two stages:
        //  * First we pick top-K samples by MAX distance. It allows to remove samples that are almost
        //    the same as given pose, but maybe has few joints bent in the other direction.
        //  * Then we pick top-K samples by MEAN distance. After outliers are removed, we pick samples
        //    that are closest by average.

        // Keeps max distance on top so we can pop it when top_k size is reached.

        // Classification is done in two stages:
        //  * First we pick top-K samples by MAX distance. It allows to remove samples that are almost
        //    the same as given pose, but maybe has few joints bent in the other direction.
        //  * Then we pick top-K samples by MEAN distance. After outliers are removed, we pick samples
        //    that are closest by average.

        // Keeps max distance on top so we can pop it when top_k size is reached.
        val maxDistances = PriorityQueue(maxDistanceTopK) { o1: Pair<PoseSample, Float?>, o2: Pair<PoseSample, Float?> ->
            -o1.second!!.compareTo(o2.second!!)
        }

        // Retrieve top K poseSamples by least distance to remove outliers.
        // Retrieve top K poseSamples by least distance to remove outliers.
        for (poseSample in poseSamples) {
            val sampleEmbedding: List<PointF3D> = poseSample.embedding
            var originalMax = 0f
            var flippedMax = 0f

            for (i in embedding.indices) {
                originalMax = max(
                    originalMax,
                    maxAbs(multiply(LandmarkPositionUtils.subtract(embedding[i], sampleEmbedding[i]), axesWeights))
                )

                flippedMax = max(
                    flippedMax,
                    maxAbs(multiply(LandmarkPositionUtils.subtract(flippedEmbedding[i], sampleEmbedding[i]), axesWeights))
                )
            }
            // Set the max distance as min of original and flipped max distance.
            maxDistances.add(
                Pair(
                    poseSample,
                    min(originalMax.toDouble(), flippedMax.toDouble()).toFloat()
                )
            )
            // We only want to retain top n so pop the highest distance.
            if (maxDistances.size > maxDistanceTopK) {
                maxDistances.poll()
            }
        }

        // Keeps higher mean distances on top so we can pop it when top_k size is reached.

        // Keeps higher mean distances on top so we can pop it when top_k size is reached.
        val meanDistances = PriorityQueue(meanDistanceTopK) { o1: Pair<PoseSample, Float?>, o2: Pair<PoseSample, Float?> ->
            -o1.second!!.compareTo(o2.second!!)
        }
        // Retrive top K poseSamples by least mean distance to remove outliers.
        // Retrive top K poseSamples by least mean distance to remove outliers.
        for (sampleDistances in maxDistances) {
            val poseSample = sampleDistances.first
            val sampleEmbedding: List<PointF3D> = poseSample.embedding
            var originalSum = 0f
            var flippedSum = 0f
            for (i in embedding.indices) {
                originalSum += sumAbs(
                    multiply(
                        LandmarkPositionUtils.subtract(embedding[i], sampleEmbedding[i]), axesWeights
                    )
                )
                flippedSum += sumAbs(
                    multiply(LandmarkPositionUtils.subtract(flippedEmbedding[i], sampleEmbedding[i]), axesWeights)
                )
            }
            // Set the mean distance as min of original and flipped mean distances.
            val meanDistance = (min(originalSum.toDouble(), flippedSum.toDouble()) / (embedding.size * 2)).toFloat()
            meanDistances.add(Pair(poseSample, meanDistance))
            // We only want to retain top k so pop the highest mean distance.
            if (meanDistances.size > meanDistanceTopK) {
                meanDistances.poll()
            }
        }

        for (sampleDistances in meanDistances) {
            val className: String = sampleDistances.first.className
            result.incrementClassConfidence(className)
        }

        return result
    }

    private fun extractPoseLandmarks(pose: Pose): List<PointF3D> {
        return pose.allPoseLandmarks.map {
            it.position3D
        }
    }

    fun confidenceRange(): Int {
        return min(maxDistanceTopK.toDouble(), meanDistanceTopK.toDouble()).toInt()
    }

    companion object {
        private const val TAG = "PoseClassifier"
        private const val MAX_DISTANCE_TOP_K = 30
        private const val MEAN_DISTANCE_TOP_K = 10

        // Note Z has a lower weight as it is generally less accurate than X & Y.
        private val AXES_WEIGHTS = PointF3D.from(1f, 1f, 0.2f)
    }
}
