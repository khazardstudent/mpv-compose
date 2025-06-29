package dev.marcelsoftware.mpvcompose

import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dev.marcelsoftware.mpvcompose.dsl.MPVObserveBuilder
import dev.marcelsoftware.mpvcompose.dsl.MPVObserverBuilder
import java.io.File

private const val TAG = "Mpv-Compose"

@Composable
fun MPVPlayer(
    modifier: Modifier = Modifier,
    configDir: String? = null,
    onBeforeInitialization: (() -> Unit)? = null,
    onInitialized: (() -> Unit)? = null,
    observedProperties: (MPVObserveBuilder.() -> Unit) = {},
    propertyObserver: (MPVObserverBuilder.() -> Unit) = {},
) {
    var initialized by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val cacheDir = context.cacheDir.absolutePath

    val gpuShaderCacheDir =
        File(cacheDir, "shader_cache")
            .apply {
                mkdirs()
            }.absolutePath

    val iccCacheDir =
        File(cacheDir, "icc_cache")
            .apply {
                mkdirs()
            }.absolutePath

    val screenshotDir =
        Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .apply {
                mkdirs()
            }.absolutePath

    DisposableEffect(Unit) {
        MPVLib.create(context)

        MPVLib.setOptionString("config", "yes")
        MPVLib.setOptionString("config-dir", configDir ?: context.dataDir.absolutePath)

        MPVLib.setOptionString("gpu-shader-cache-dir", gpuShaderCacheDir)
        MPVLib.setOptionString("icc-cache-dir", iccCacheDir)

        MPVLib.setOptionString("screenshot-directory", screenshotDir)

        onBeforeInitialization?.invoke()

        MPVLib.init()

        initialized = true

        MPVObserveBuilder().observedProperties()
        MPVObserverBuilder().apply {
            propertyObserver()
            build()
        }

        onDispose {
            if (initialized) {
                MPVLib.destroy()
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(
                    object : SurfaceHolder.Callback {
                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int,
                        ) {
                            if (initialized) {
                                MPVLib.logMessage(TAG, MPVLib.LogLevel.MPV_LOG_LEVEL_DEBUG, "Surface changed: ${width}x$height")
                                MPVLib.setPropertyString("android-surface-size", "${width}x$height")
                            }
                        }

                        override fun surfaceCreated(p0: SurfaceHolder) {
                            if (initialized) {
                                MPVLib.logMessage(TAG, MPVLib.LogLevel.MPV_LOG_LEVEL_DEBUG, "Attaching surface")
                                MPVLib.attachSurface(holder.surface)
                                MPVLib.setOptionString("force-window", "yes")
                                onInitialized?.invoke()
                            }
                        }

                        override fun surfaceDestroyed(p0: SurfaceHolder) {
                            MPVLib.logMessage(TAG, MPVLib.LogLevel.MPV_LOG_LEVEL_DEBUG, "Detaching surface")
                            MPVLib.setPropertyString("vo", "null")
                            MPVLib.setPropertyString("force-window", "no")
                            MPVLib.detachSurface()
                        }
                    },
                )
            }
        },
    )
}
