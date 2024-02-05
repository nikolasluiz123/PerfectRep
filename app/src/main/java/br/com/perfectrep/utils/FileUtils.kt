package br.com.perfectrep.utils

import br.com.perfectrep.extractor.enums.EnumImageFileType
import java.io.File
import java.io.FileNotFoundException

/**
 * Object utilitário para realização de operações diversas com arquivos.
 */
object FileUtils {

    /**
     * Função que verifica se o diretório especificado existe.
     *
     * @param directory Diretório que deve ser validado.
     */
    fun directoryExists(directory: File) = directory.exists() && directory.isDirectory

    /**
     * Função que recupera uma lista de [File] de dentro de um diretório
     * especificado.
     *
     * @param directory Diretório que deseja recuperar os objetos [File].
     * @param type Tipo do arquivo que deve ser recuperado.
     *
     * @throws FileNotFoundException Lançada se não for encontrado o diretório especificado em [directory].
     */
    fun getFilesFrom(directory: File, type: EnumImageFileType): List<File> {
        if (!directoryExists(directory)) {
            throw FileNotFoundException("Não foi encontrado o diretório no path: ${directory.absolutePath}.")
        }

        return directory.listFiles { file ->
            file.isFile && file.extension.equals(type.toString(), ignoreCase = true)
        }?.toList() ?: emptyList()
    }

    /**
     * Função pra remover arquivos de um tipo específico de um diretório.
     *
     * @param directory Diretório que terá os arquivos deletados.
     * @param type Tipo dos arquivos que devem sere deletados.
     *
     * @throws FileNotFoundException Lançada se não for encontrado o diretório especificado em [directory].
     */
    fun deleteFilesFrom(directory: String, type: EnumImageFileType) {
        val file = File(directory)

        if (!directoryExists(file)) {
            throw FileNotFoundException("Não foi encontrado o diretório no path: ${directory}.")
        }

        getFilesFrom(file, type).forEach(File::delete)
    }
}