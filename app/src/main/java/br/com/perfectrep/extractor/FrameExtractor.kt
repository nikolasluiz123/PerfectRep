package br.com.perfectrep.extractor

import android.util.Log
import br.com.perfectrep.utils.FileUtils
import br.com.perfectrep.enums.EnumFrameExtractorOptionKeys
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Classe responsável por extrair os frames de um vídeo.
 */
class FrameExtractor(private val options: FrameExtractorOptions) {

    /**
     * Função responsável por extrair os frames do vídeo e salvar em um diretório.
     *
     * @param onSuccess Callback chamado quando a execução retornar o código de sucesso [RETURN_CODE_SUCCESS].
     */
    suspend fun extractFrames(onSuccess: suspend () -> Unit) = withContext(IO) {
        val commands = getCommands()

        FFmpeg.executeAsync(commands) { executionId, code ->
            when (code) {
                RETURN_CODE_SUCCESS -> {
                    CoroutineScope(IO).launch {
                        onSuccess()
                    }
                }
                RETURN_CODE_CANCEL -> {
                    Log.i("VideoFrameExtractor", "A execução de id $executionId foi cancelada. Todos os frames gerados serão deletados.")
                    FileUtils.deleteFilesFrom(options.outputDirectoryPath, options.outputFileFormat)
                }
                else -> {
                    Log.e("VideoFrameExtractor", "Erro ao extrair frames. Código de retorno: $code")
                }
            }
        }
    }

    /**
     * Função que retorna um array contendo as especificações da
     * extração dos frames.
     */
    private fun getCommands(): Array<String> {
        val commands = mutableListOf<String>()

        commands.add(EnumFrameExtractorOptionKeys.INPUT_FILE_PATH.value)
        commands.add(options.inputFilePath)

        if (options.overrideFiles) {
            commands.add(EnumFrameExtractorOptionKeys.OVERRIDE_EXISTING_FILES.value)
        }

        commands.add("${options.outputDirectoryPath}/${options.outputFileName}_%08d.${options.outputFileFormat.name.lowercase()}")

        return commands.toTypedArray()
    }
}