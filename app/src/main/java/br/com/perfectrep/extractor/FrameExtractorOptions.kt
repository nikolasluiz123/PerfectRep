package br.com.perfectrep.extractor

import br.com.perfectrep.extractor.enums.EnumImageFileType

/**
 * Classe com os valores das configurações da extração de frames.
 */
data class FrameExtractorOptions(
    val inputFilePath: String,
    val outputDirectoryPath: String,
    val outputFileName: String,
    val outputFileFormat: EnumImageFileType,
    val overrideFiles: Boolean = true,
    val frameRate: Int = 30
)
