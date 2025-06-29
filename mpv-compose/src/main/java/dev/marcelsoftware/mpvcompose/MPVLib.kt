package dev.marcelsoftware.mpvcompose

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import dev.marcelsoftware.mpvcompose.dsl.MPVObserveBuilder
import dev.marcelsoftware.mpvcompose.dsl.MPVObserverBuilder

@Suppress("unused")
object MPVLib {
    init {
        System.loadLibrary("mpv")
        System.loadLibrary("mpv-compose")
    }

    @JvmStatic
    external fun create(appctx: Context)

    @JvmStatic
    external fun init()

    @JvmStatic
    external fun destroy()

    @JvmStatic
    external fun attachSurface(surface: Surface?)

    @JvmStatic
    external fun detachSurface()

    @JvmStatic
    external fun command(cmd: Array<String?>)

    @JvmStatic
    external fun setOptionString(
        name: String,
        value: String,
    ): Int

    @JvmStatic
    external fun grabThumbnail(dimension: Int): Bitmap?

    // FIXME: get methods are actually nullable
    @JvmStatic
    external fun getPropertyInt(property: String): Int?

    @JvmStatic
    external fun setPropertyInt(
        property: String,
        value: Int,
    )

    @JvmStatic
    external fun getPropertyDouble(property: String): Double?

    @JvmStatic
    external fun setPropertyDouble(
        property: String,
        value: Double,
    )

    @JvmStatic
    external fun getPropertyBoolean(property: String): Boolean?

    @JvmStatic
    external fun setPropertyBoolean(
        property: String,
        value: Boolean,
    )

    @JvmStatic
    external fun getPropertyString(property: String): String?

    @JvmStatic
    external fun setPropertyString(
        property: String,
        value: String,
    )

    @JvmStatic
    external fun observeProperty(
        property: String,
        format: Int,
    )

    @JvmStatic
    fun observeProperties(builder: MPVObserveBuilder.() -> Unit) = MPVObserveBuilder().builder()

    private val observers: MutableList<EventObserver> = ArrayList<EventObserver>()

    @JvmStatic
    fun addObserver(o: EventObserver?) {
        synchronized(observers) { observers.add(o!!) }
    }

    @JvmStatic
    fun addObserver(builder: MPVObserverBuilder.() -> Unit) =
        MPVObserverBuilder().apply {
            builder
            build()
        }

    @JvmStatic
    fun removeObserver(o: EventObserver?) {
        synchronized(observers) { observers.remove(o) }
    }

    @JvmStatic
    fun eventProperty(
        property: String,
        value: Long,
    ) {
        synchronized(observers) {
            for (o in observers) o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(
        property: String,
        value: Boolean,
    ) {
        synchronized(observers) {
            for (o in observers) o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(
        property: String,
        value: Double,
    ) {
        synchronized(observers) {
            for (o in observers) o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(
        property: String,
        value: String,
    ) {
        synchronized(observers) {
            for (o in observers) o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(property: String) {
        synchronized(observers) {
            for (o in observers) o.eventProperty(property)
        }
    }

    @JvmStatic
    fun event(eventId: Int) {
        synchronized(observers) {
            for (o in observers) o.event(eventId)
        }
    }

    private val log_observers: MutableList<LogObserver> = ArrayList<LogObserver>()

    @JvmStatic
    fun addLogObserver(o: LogObserver?) {
        synchronized(log_observers) { log_observers.add(o!!) }
    }

    @JvmStatic
    fun removeLogObserver(o: LogObserver?) {
        synchronized(log_observers) { log_observers.remove(o) }
    }

    @JvmStatic
    fun logMessage(
        prefix: String,
        level: Int,
        text: String,
    ) {
        synchronized(log_observers) {
            for (o in log_observers) o.logMessage(prefix, level, text)
        }
    }

    interface EventObserver {
        fun eventProperty(property: String)

        fun eventProperty(
            property: String,
            value: Long,
        )

        fun eventProperty(
            property: String,
            value: Boolean,
        )

        fun eventProperty(
            property: String,
            value: String,
        )

        fun eventProperty(
            property: String,
            value: Double,
        )

        fun event(eventId: Int)
    }

    interface LogObserver {
        fun logMessage(
            prefix: String,
            level: Int,
            text: String,
        )
    }

    object Format {
        const val MPV_FORMAT_NONE: Int = 0
        const val MPV_FORMAT_STRING: Int = 1
        const val MPV_FORMAT_OSD_STRING: Int = 2
        const val MPV_FORMAT_FLAG: Int = 3
        const val MPV_FORMAT_INT64: Int = 4
        const val MPV_FORMAT_DOUBLE: Int = 5
        const val MPV_FORMAT_NODE: Int = 6
        const val MPV_FORMAT_NODE_ARRAY: Int = 7
        const val MPV_FORMAT_NODE_MAP: Int = 8
        const val MPV_FORMAT_BYTE_ARRAY: Int = 9
    }

    object MPVEventId {
        const val MPV_EVENT_NONE: Int = 0
        const val MPV_EVENT_SHUTDOWN: Int = 1
        const val MPV_EVENT_LOG_MESSAGE: Int = 2
        const val MPV_EVENT_GET_PROPERTY_REPLY: Int = 3
        const val MPV_EVENT_SET_PROPERTY_REPLY: Int = 4
        const val MPV_EVENT_COMMAND_REPLY: Int = 5
        const val MPV_EVENT_START_FILE: Int = 6
        const val MPV_EVENT_END_FILE: Int = 7
        const val MPV_EVENT_FILE_LOADED: Int = 8

        @Deprecated("")
        const val MPV_EVENT_IDLE: Int = 11

        @Deprecated("")
        const val MPV_EVENT_TICK: Int = 14
        const val MPV_EVENT_CLIENT_MESSAGE: Int = 16
        const val MPV_EVENT_VIDEO_RECONFIG: Int = 17
        const val MPV_EVENT_AUDIO_RECONFIG: Int = 18
        const val MPV_EVENT_SEEK: Int = 20
        const val MPV_EVENT_PLAYBACK_RESTART: Int = 21
        const val MPV_EVENT_PROPERTY_CHANGE: Int = 22
        const val MPV_EVENT_QUEUE_OVERFLOW: Int = 24
        const val MPV_EVENT_HOOK: Int = 25
    }

    object LogLevel {
        const val MPV_LOG_LEVEL_NONE: Int = 0
        const val MPV_LOG_LEVEL_FATAL: Int = 10
        const val MPV_LOG_LEVEL_ERROR: Int = 20
        const val MPV_LOG_LEVEL_WARN: Int = 30
        const val MPV_LOG_LEVEL_INFO: Int = 40
        const val MPV_LOG_LEVEL_V: Int = 50
        const val MPV_LOG_LEVEL_DEBUG: Int = 60
        const val MPV_LOG_LEVEL_TRACE: Int = 70
    }
}
