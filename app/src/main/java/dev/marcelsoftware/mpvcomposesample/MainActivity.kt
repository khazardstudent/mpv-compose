package dev.marcelsoftware.mpvcomposesample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.marcelsoftware.mpvcompose.DefaultLogObserver
import dev.marcelsoftware.mpvcompose.MPVLib
import dev.marcelsoftware.mpvcompose.MPVPlayer
import dev.marcelsoftware.mpvcomposesample.ui.theme.MpvComposeSampleTheme
import java.io.File
import java.io.FileInputStream
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MpvComposeSample"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var file by remember { mutableStateOf("") }

            MpvComposeSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    AnimatedVisibility(
                        file.isEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        MainScreen(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                            onFileOpen = { path ->
                                file = path
                            },
                        )
                    }
                    AnimatedVisibility(
                        file.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        PlayerScreen(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                            filePath = file,
                            onPlaybackFinished = { file = "" },
                        )
                    }
                }
            }

            BackHandler {
                if (file.isNotEmpty()) {
                    file = ""
                } else {
                    exitProcess(0)
                }
            }
        }
    }

    @Composable
    private fun PlayerScreen(
        modifier: Modifier = Modifier,
        filePath: String,
        onPlaybackFinished: () -> Unit = {},
    ) {
        var showControls by remember { mutableStateOf(false) }

        var playBackDuration by remember { mutableLongStateOf(1L) }
        var playBackProgress by remember { mutableLongStateOf(1L) }

        MPVPlayer(
            modifier =
                modifier
                    .fillMaxSize()
                    .clickable { showControls = true },
            onInitialized = {
                MPVLib.addLogObserver(DefaultLogObserver()) // Initialize default log observer

                MPVLib.setPropertyBoolean("keep-open", true) // Keep mpv open after the video ends

                MPVLib.setPropertyString("vo", "gpu") // Set vo

                MPVLib.command(arrayOf("loadfile", filePath)) // load file
            },
            // Set properties to be observed
            observedProperties = {
                long("duration") // observe duration
                int("time-pos") // observe playback position
                boolean("eof-reached") // observe end of file (requires keep-open boolean propriety
            },
            // Observe properties
            propertyObserver = {
                // Observe duration
                long("duration") { duration ->
                    MPVLib.logMessage(TAG, MPVLib.LogLevel.MPV_LOG_LEVEL_DEBUG, "Video duration changed : $duration")
                    playBackDuration = duration
                }

                // Observe playback position
                long("time-pos") { timePos ->
                    MPVLib.logMessage(TAG, MPVLib.LogLevel.MPV_LOG_LEVEL_DEBUG, "Video playback position changed : ${timePos.toFloat()}")
                    playBackProgress = timePos
                }

                // Observe EOF
                boolean("eof-reached") { eof: Boolean ->
                    if (eof) {
                        onPlaybackFinished.invoke()
                    }
                }
            },
        )

        AnimatedVisibility(
            showControls,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable { showControls = false },
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart),
                ) {
                    Text(
                        text = formatDuration(playBackProgress),
                    )

                    Slider(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        value = (playBackProgress.toFloat() / playBackDuration).coerceIn(0F, 1F),
                        onValueChange = {
                            MPVLib.setPropertyInt("time-pos", (it * playBackDuration).toInt())
                        },
                    )

                    Text(
                        text = formatDuration(playBackDuration),
                    )
                }
            }
        }
    }

    @Composable
    fun MainScreen(
        modifier: Modifier = Modifier,
        onFileOpen: (String) -> Unit,
    ) {
        val launcher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->

                        this.contentResolver.openFileDescriptor(uri, "r")?.detachFd()?.let { fd: Int ->
                            val path = File("/proc/self/fd/$fd").canonicalPath
                            if (!path.startsWith("/proc") && File(path).canRead()) {
                                val ins = FileInputStream(path)
                                ins.read()
                                ParcelFileDescriptor.adoptFd(fd).close()
                                onFileOpen.invoke(path)
                            } else {
                                onFileOpen("fd://$fd")
                            }
                        }
                    }
                }
            }

        Box(
            modifier = modifier,
        ) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    val intent =
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "video/*"
                        }
                    launcher.launch(intent)
                },
            ) {
                Text(
                    text = "Open File",
                )
            }
        }
    }

    private fun formatDuration(duration: Long): String {
        val seconds = duration % 60
        val minutes = (duration / 60) % 60
        val hours = duration / 3600

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
