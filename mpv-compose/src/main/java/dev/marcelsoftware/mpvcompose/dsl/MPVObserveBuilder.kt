package dev.marcelsoftware.mpvcompose.dsl

import dev.marcelsoftware.mpvcompose.MPVLib

@MPVDsl
class MPVObserveBuilder {
    fun string(property: String) = MPVLib.observeProperty(property, MPVLib.Format.MPV_FORMAT_STRING)

    fun osdString(property: String) = MPVLib.observeProperty(property, MPVLib.Format.MPV_FORMAT_OSD_STRING)

    fun boolean(property: String) = MPVLib.observeProperty(property, MPVLib.Format.MPV_FORMAT_FLAG)

    fun int64(property: String) = MPVLib.observeProperty(property, MPVLib.Format.MPV_FORMAT_INT64)

    fun long(property: String) = int64(property)

    fun int(property: String) = long(property)

    fun double(property: String) = MPVLib.observeProperty(property, MPVLib.Format.MPV_FORMAT_DOUBLE)

    fun none(property: String) = MPVLib.observeProperty(property, MPVLib.Format.MPV_FORMAT_NONE)

    fun node(property: String): Nothing = TODO("Not implemented")

    fun array(property: String): Nothing = TODO("Not implemented")

    fun map(property: String): Nothing = TODO("Not implemented")

    fun byteArray(property: String): Nothing = TODO("Not implemented")
}
