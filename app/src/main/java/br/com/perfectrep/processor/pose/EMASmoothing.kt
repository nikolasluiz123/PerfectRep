package br.com.perfectrep.processor.pose

import android.os.SystemClock

class EMASmoothing(
    private val windowSize: Int = DEFAULT_WINDOW_SIZE,
    private val alpha: Float = DEFAULT_ALPHA
) {

    private val window: ArrayDeque<ClassificationResult> = ArrayDeque()
    private var lastInputMs: Long = 0

    fun getSmoothedResult(result: ClassificationResult): ClassificationResult {
        val nowMs = SystemClock.elapsedRealtime()

        if (nowMs - lastInputMs > RESET_THRESHOLD_MS) {
            window.clear()
        }

        lastInputMs = nowMs

        if (window.size == windowSize) {
            window.removeLast()
        }

        window.addFirst(result)

        val allClasses: MutableSet<String> = HashSet()
        window.forEach { allClasses.addAll(it.getAllClasses()) }

        val smoothedResult = ClassificationResult()

        allClasses.forEach { className ->
            var factor = 1f
            var topSum = 0f
            var bottomSum = 0f

            for (r in window) {
                val value = r.getClassConfidence(className)
                topSum += factor * value
                bottomSum += factor
                factor = (factor * (1.0 - alpha)).toFloat()
            }

            smoothedResult.putClassConfidence(className = className, confidence = topSum / bottomSum)
        }

        return smoothedResult
    }

    companion object {
        private const val DEFAULT_WINDOW_SIZE = 10
        private const val DEFAULT_ALPHA = 0.2f
        private const val RESET_THRESHOLD_MS: Long = 100
    }
}