package br.com.perfectrep.processor.pose


class RepetitionCounter @JvmOverloads constructor(
    val className: String,
    private val enterThreshold: Float = DEFAULT_ENTER_THRESHOLD,
    private val exitThreshold: Float = DEFAULT_EXIT_THRESHOLD
) {
    var numRepeats = 0
        private set

    private var poseEntered = false

    /**
     * Adds a new Pose classification result and updates reps for given class.
     *
     * @param classificationResult {link ClassificationResult} of class to confidence values.
     * @return number of reps.
     */
    fun addClassificationResult(classificationResult: ClassificationResult): Int {
        val poseConfidence: Float = classificationResult.getClassConfidence(className)
        if (!poseEntered) {
            poseEntered = poseConfidence > enterThreshold
            return numRepeats
        }
        if (poseConfidence < exitThreshold) {
            numRepeats++
            poseEntered = false
        }
        return numRepeats
    }

    companion object {
        // These thresholds can be tuned in conjunction with the Top K values in {@link PoseClassifier}.
        // The default Top K value is 10 so the range here is [0-10].
        private const val DEFAULT_ENTER_THRESHOLD = 6f
        private const val DEFAULT_EXIT_THRESHOLD = 4f
    }
}

