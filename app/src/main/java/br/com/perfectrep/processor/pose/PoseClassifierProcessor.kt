package br.com.perfectrep.processor.pose

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Environment
import android.os.Looper
import br.com.perfectrep.processor.pose.enums.EnumPoseClasses
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale

class PoseClassifierProcessor(
    private val poseClasses: List<EnumPoseClasses>,
    private val isStreamMode: Boolean,
    private val context: Context
) {

    private lateinit var poseClassifier: PoseClassifier
    private lateinit var emaSmoothing: EMASmoothing
    private lateinit var repCounters: MutableList<RepetitionCounter>

    private var lastRepResult: String = ""

    suspend fun loadPoseSamples() = withContext(IO) {
        val samples = getPoseSampleOfFile()

        poseClassifier = PoseClassifier(poseSamples = samples)

        if (isStreamMode) {
            emaSmoothing = EMASmoothing()
            repCounters = mutableListOf()

            for (poseClass in poseClasses) {
                repCounters.add(RepetitionCounter(poseClass.up))
                repCounters.add(RepetitionCounter(poseClass.down))
            }
        }
    }

    private suspend fun getPoseSampleOfFile(): MutableList<PoseSample> = withContext(IO) {
        val samples = mutableListOf<PoseSample>()
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), POSE_SAMPLES_FILE)

        if (!file.exists()) {
            throw FileNotFoundException("Não foi encontrado o arquivo no path: ${file.path}.")
        }

        file.useLines {
            it.forEach { line ->
                val poseSample = PoseSample.getPoseSample(line)

                if (poseSample != null) {
                    samples.add(poseSample)
                }
            }
        }

        samples
    }

    suspend fun getPoseResult(pose: Pose): List<String> = withContext(IO) {
        check(Looper.myLooper() != Looper.getMainLooper())
        val result = mutableListOf<String>()
        var classification: ClassificationResult = poseClassifier.classify(pose)

        // Update {@link RepetitionCounter}s if {@code isStreamMode}.

        // Update {@link RepetitionCounter}s if {@code isStreamMode}.
        if (isStreamMode) {
            // Feed pose to smoothing even if no pose found.
            classification = emaSmoothing.getSmoothedResult(classification)

            // Return early without updating repCounter if no pose found.
            if (pose.allPoseLandmarks.isEmpty()) {
                result.add(lastRepResult)
                return@withContext result
            }
            for (repCounter in repCounters) {
                val repsBefore: Int = repCounter.numRepeats
                val repsAfter = repCounter.addClassificationResult(classification)
                if (repsAfter > repsBefore) {
                    // Play a fun beep when rep counter updates.
                    val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP)
                    lastRepResult = REP_RESULT_PATTERN.format(repCounter.className, repsAfter)

                    break
                }
            }
            result.add(lastRepResult)
        }

        // Add maxConfidence class of current frame to result if pose is found.

        // Add maxConfidence class of current frame to result if pose is found.
        if (pose.allPoseLandmarks.isNotEmpty()) {
            val maxConfidenceClass = classification.getMaxConfidenceClass()
            val maxConfidenceClassResult = CONFIDENCE_PATTERN.format(
                CONFIDENCE_PATTERN,
                maxConfidenceClass,
                classification.getClassConfidence(maxConfidenceClass) / poseClassifier.confidenceRange()
            )
            result.add(maxConfidenceClassResult)
        }

        return@withContext result
    }

    companion object {
        private const val POSE_SAMPLES_FILE = "fitness_poses_csvs_out.csv"

        private const val REP_RESULT_PATTERN = "%s : %d reps"
        private const val CONFIDENCE_PATTERN = "%s : %.2f confidence"
    }
}