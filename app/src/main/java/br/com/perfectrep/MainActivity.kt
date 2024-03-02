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
import br.com.perfectrep.extractor.enums.EnumImageFileType
import br.com.perfectrep.extractor.FrameExtractor
import br.com.perfectrep.extractor.FrameExtractorOptions
import br.com.perfectrep.processor.ObjectDetectorProcessor
import br.com.perfectrep.processor.pose.PoseDetectorProcessor
import br.com.perfectrep.processor.UriProcessor
import br.com.perfectrep.processor.pose.PoseClassifierProcessor
import br.com.perfectrep.processor.pose.enums.EnumPoseClasses
import br.com.perfectrep.ui.theme.PerfectrepTheme
import br.com.perfectrep.validator.BenchPressValidator
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

private const val VIDEO_FILE_EXTENSION = "mp4"
private const val FRAMES_FOLDER_NAME = "supino_inclinado_halter"

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

                val videos = moviesDirectory?.listFiles { file ->
                    file.isFile && file.extension.equals(VIDEO_FILE_EXTENSION, ignoreCase = true)
                }?.toList() ?: emptyList()

                val framesFolder = File(moviesDirectory, FRAMES_FOLDER_NAME)

                if (!framesFolder.exists()) {
                    framesFolder.mkdir()
                }

                LaunchedEffect(null) {
                    coroutineScope.launch {
//                        PoseClassifierProcessor(EnumPoseClasses.BENCH_PRESS, context).loadPoseSamples()

//                        val extractorOptions = FrameExtractorOptions(
//                            inputFilePath = videos.first().absolutePath,
//                            outputDirectoryPath = framesFolder.absolutePath,
//                            outputFileName = "temp_frame",
//                            outputFileFormat = EnumImageFileType.PNG
//                        )
//
//                        val frameExtractor = FrameExtractor(extractorOptions)
//
//                        val poseProcessor = PoseDetectorProcessor()
//
//                        // Coisas Relevantes para Validar um Supino.
//                        val landMarks = listOf(
//                            PoseLandmark.RIGHT_ELBOW,
//                            PoseLandmark.LEFT_ELBOW,
//                            PoseLandmark.RIGHT_SHOULDER,
//                            PoseLandmark.LEFT_SHOULDER,
//                            PoseLandmark.RIGHT_WRIST,
//                            PoseLandmark.LEFT_WRIST
//                        )
//
//                        val validator = BenchPressValidator()
//
//                        frameExtractor.extractFrames {
//                            val uriProcessor = UriProcessor(context = context, extractorOptions = extractorOptions)
//
//                            uriProcessor.processFramesAsync { inputImage, file ->
//                                poseProcessor.process(
//                                    inputImage = inputImage,
//                                    onSuccess = {  },
//                                    onFailure = {
//                                        Log.e("Teste", "Houve um erro ao processar o InputImage para detectar a pose.", it)
//                                    },
//                                    onComplete = {
//                                        Log.i("Teste", "Arquivo ${file.path} será deletado")
//                                        file.delete()
//                                    }
//                                )
//                            }
//                        }
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