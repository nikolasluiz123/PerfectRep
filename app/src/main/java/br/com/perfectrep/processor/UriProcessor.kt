package br.com.perfectrep.processor

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import br.com.perfectrep.utils.FileUtils
import br.com.perfectrep.extractor.enums.EnumImageFileType
import br.com.perfectrep.extractor.FrameExtractorOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

/**
 * Classe responsável por processar a lista de Uris que representam os arquivos de frames.
 */
class UriProcessor(
    private val context: Context,
    private val extractorOptions: FrameExtractorOptions
) {

    /**
     * Método que processa os frames de forma assíncrona.
     *
     * @param onSuccess Callback executado quando obtiver sucesso em converter a [Uri] em [InputImage].
     *
     * @throws FileNotFoundException Na recuperação da lista de uris pode ser lançada por [retrieveFrameUris].
     */
    @Throws(FileNotFoundException::class)
    suspend fun processFramesAsync(onSuccess: (InputImage, File) -> Unit) = withContext(IO) {
        val uriList = retrieveFrameUris(
            directoryPath = extractorOptions.outputDirectoryPath,
            fileType = extractorOptions.outputFileFormat
        )

        uriList.forEach { uri ->
            Log.i("Teste", "processFramesAsync: Processando a Uri: $uri")
            onSuccess(InputImage.fromFilePath(context, uri), uri.toFile())
        }
    }

    /**
     * Método que acessa o diretório e recupera os arquivos de imagem
     * criados como uma lista de [Uri].
     *
     * @param directoryPath Diretório onde estão os frames em forma de arquivos de imagem.
     * @param fileType Tipo do arquivo de imagem que deve ser transformado em [Uri]
     *
     * @throws FileNotFoundException Será lançada quando não for encotrado um diretório no path indicado em [directoryPath].
     *
     * @throws FileNotFoundException Será lançada quando não houver arquivos no diretório especificado em [directoryPath]
     * do tipo específicado em [fileType].
     */
    @Throws(FileNotFoundException::class)
    private fun retrieveFrameUris(directoryPath: String, fileType: EnumImageFileType): List<Uri> {
        val directory = File(directoryPath)

        if (!FileUtils.directoryExists(directory)) {
            throw FileNotFoundException("O diretório $directory não foi encontrado.")
        }

        val frameFiles = FileUtils.getFilesFrom(directory = directory, type = fileType)

        if (frameFiles.isEmpty()) {
            throw FileNotFoundException("No diretório $directory não foram encontrados arquivos do tipo ${fileType}.")
        }

        return frameFiles.map { it.toUri() }
    }
}