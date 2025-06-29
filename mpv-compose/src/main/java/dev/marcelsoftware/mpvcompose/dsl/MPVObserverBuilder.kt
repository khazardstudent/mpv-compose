package dev.marcelsoftware.mpvcompose.dsl

import dev.marcelsoftware.mpvcompose.MPVLib

@MPVDsl
class MPVObserverBuilder {
    private val propertyStringHandlers = mutableMapOf<String, (String) -> Unit>()
    private val propertyLongHandlers = mutableMapOf<String, (Long) -> Unit>()
    private val propertyBooleanHandlers = mutableMapOf<String, (Boolean) -> Unit>()
    private val propertyDoubleHandlers = mutableMapOf<String, (Double) -> Unit>()
    private val eventHandlers = mutableMapOf<Int, () -> Unit>()
    private val eventHandlers1 = mutableMapOf<String, () -> Unit>()

    @JvmName("onPropertyString")
    fun string(
        property: String,
        handler: (String) -> Unit,
    ) {
        propertyStringHandlers[property] = handler
    }

    @JvmName("onPropertyLong")
    fun long(
        property: String,
        handler: (Long) -> Unit,
    ) {
        propertyLongHandlers[property] = handler
    }

    @Deprecated(
        message = "Use the long method instead for parsing int values.",
        replaceWith = ReplaceWith("long(property)"),
        level = DeprecationLevel.ERROR,
    )
    fun int(
        property: String,
        handler: (Int) -> Unit,
    ) {}

    @JvmName("onPropertyBoolean")
    fun boolean(
        property: String,
        handler: (Boolean) -> Unit,
    ) {
        propertyBooleanHandlers[property] = handler
    }

    @JvmName("onPropertyDouble")
    fun double(
        property: String,
        handler: (Double) -> Unit,
    ) {
        propertyDoubleHandlers[property] = handler
    }

    fun event(
        eventId: Int,
        handler: () -> Unit,
    ) {
        eventHandlers[eventId] = handler
    }

    @JvmName("onEventString")
    fun event(
        property: String,
        handler: () -> Unit,
    ) {
        eventHandlers1[property] = handler
    }

    internal fun build(): MPVLib.EventObserver {
        val observer =
            object : MPVLib.EventObserver {
                override fun eventProperty(property: String) {
                    eventHandlers1[property]?.invoke()
                }

                override fun eventProperty(
                    property: String,
                    value: Long,
                ) {
                    propertyLongHandlers[property]?.invoke(value)
                }

                override fun eventProperty(
                    property: String,
                    value: Boolean,
                ) {
                    propertyBooleanHandlers[property]?.invoke(value)
                }

                override fun eventProperty(
                    property: String,
                    value: String,
                ) {
                    propertyStringHandlers[property]?.invoke(value)
                }

                override fun eventProperty(
                    property: String,
                    value: Double,
                ) {
                    propertyDoubleHandlers[property]?.invoke(value)
                }

                override fun event(eventId: Int) {
                    eventHandlers[eventId]?.invoke()
                }

                fun remove() {
                    MPVLib.removeObserver(this)
                }
            }

        MPVLib.addObserver(observer)

        return observer
    }
}
