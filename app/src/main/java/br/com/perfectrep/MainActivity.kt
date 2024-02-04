package br.com.perfectrep

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import br.com.perfectrep.enums.EnumImageFileType
import br.com.perfectrep.extractor.FrameExtractor
import br.com.perfectrep.extractor.FrameExtractorOptions
import br.com.perfectrep.processor.ObjectDetectorProcessor
import br.com.perfectrep.processor.PoseDetectorProcessor
import br.com.perfectrep.processor.UriProcessor
import br.com.perfectrep.ui.theme.PerfectrepTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PerfectrepTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }

                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val moviesDirectory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                val framesDirectory = File(moviesDirectory, "frames")
                val video = File(moviesDirectory, "supino_inclinado_halter_cortado.mp4")

                if (!framesDirectory.exists()) {
                    framesDirectory.mkdir()
                    Log.i("Teste", "A pasta de frames foi criada.")
                } else {
                    Log.i("Teste", "A pasta de frames já existia ${framesDirectory.absolutePath}")
                }

                LaunchedEffect(null) {
                    coroutineScope.launch {
                        val extractorOptions = FrameExtractorOptions(
                            inputFilePath = video.absolutePath,
                            outputDirectoryPath = framesDirectory.absolutePath,
                            outputFileName = "temp_frame",
                            outputFileFormat = EnumImageFileType.PNG
                        )

                        val frameExtractor = FrameExtractor(extractorOptions)

                        val poseProcessor = PoseDetectorProcessor()

                        frameExtractor.extractFrames {
                            val uriProcessor = UriProcessor(context = context, extractorOptions = extractorOptions)

                            uriProcessor.processFramesAsync { inputImage, file ->
                                poseProcessor.process(
                                    inputImage = inputImage,
                                    onSuccess = {

                                    },
                                    onFailure = {
                                        Log.e("Teste", "Houve um erro ao processar o InputImage para detectar a pose.", it)
                                    },
                                    onComplete = {
                                        Log.i("Teste", "Arquivo ${file.path} será deletado")
                                        file.delete()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DetectObjects(
        coroutineScope: CoroutineScope,
        video: File,
        framesDirectory: File,
        context: Context
    ) {
        LaunchedEffect(null) {
            coroutineScope.launch {
                val extractorOptions = FrameExtractorOptions(
                    inputFilePath = video.absolutePath,
                    outputDirectoryPath = framesDirectory.absolutePath,
                    outputFileName = "temp_frame",
                    outputFileFormat = EnumImageFileType.PNG
                )

                val frameExtractor = FrameExtractor(extractorOptions)

                frameExtractor.extractFrames {
                    val uriProcessor = UriProcessor(context = context, extractorOptions = extractorOptions)

                    val objectDetectorProcessor = ObjectDetectorProcessor()

                    uriProcessor.processFramesAsync { inputImage, file ->
                        objectDetectorProcessor.process(
                            inputImage = inputImage,
                            onSuccess = {

                            },
                            onFailure = {
                                Log.e("Teste", "Houve um erro ao processar o InputImage para detectar os objetos.", it)
                            },
                            onComplete = {
                                Log.i("Teste", "Arquivo ${file.path} será deletado")
                                file.delete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PerfectrepTheme {
        Greeting("Android")
    }
}