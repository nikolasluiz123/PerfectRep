package br.com.perfectrep.validator

import br.com.perfectrep.validator.enums.EnumAccuracyLevel

data class ValidationResult(
    val accuracyLevel: EnumAccuracyLevel,
    val orientation: String
)
