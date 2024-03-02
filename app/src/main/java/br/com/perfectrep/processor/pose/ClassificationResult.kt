package br.com.perfectrep.processor.pose

class ClassificationResult(
    private var classConfidences: MutableMap<String, Float> = mutableMapOf()
) {

    fun getAllClasses(): Set<String> = classConfidences.keys

    fun getClassConfidence(className: String): Float = classConfidences.getOrDefault(className, 0F)

    fun getMaxConfidenceClass(): String {
        return classConfidences.maxBy(Map.Entry<String, Float>::value).key
    }

    fun incrementClassConfidence(className: String) {
        if (classConfidences.containsKey(className)) {
            classConfidences[className] = classConfidences[className]!! + 1
        } else {
            classConfidences[className] = classConfidences[className]!!
        }
    }

    fun putClassConfidence(className: String, confidence: Float) {
        classConfidences[className] = confidence
    }

}