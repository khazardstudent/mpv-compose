cmake_minimum_required(VERSION 3.22.1)
project("mpv-compose")

# Set PREFIX based on architecture
if(ANDROID_ABI STREQUAL "armeabi-v7a")
    set(PREFIX ${PREFIX32})
elseif(ANDROID_ABI STREQUAL "arm64-v8a")
    set(PREFIX ${PREFIX64})
elseif(ANDROID_ABI STREQUAL "x86_64")
    set(PREFIX ${PREFIX_X64})
elseif(ANDROID_ABI STREQUAL "x86")
    set(PREFIX ${PREFIX_X86})
endif()

# FFmpeg and MPV libraries
set(PREBUILT_LIBS
        swresample
        postproc
        avutil
        avcodec
        avformat
        swscale
        avfilter
        avdevice
        mpv
)

foreach(LIB ${PREBUILT_LIBS})
    if(EXISTS "${PREFIX}/lib/lib${LIB}.so")
        add_library(${LIB} SHARED IMPORTED)
        set_target_properties(${LIB} PROPERTIES IMPORTED_LOCATION "${PREFIX}/lib/lib${LIB}.so")
        if(LIB STREQUAL "avdevice" OR LIB STREQUAL "mpv")
            target_include_directories(${LIB} INTERFACE "${PREFIX}/include")
        endif()
    endif()
endforeach()

# Main library
add_library(mpv-compose SHARED
        main.cpp
        render.cpp
        log.cpp
        jni_utils.cpp
        property.cpp
        event.cpp
        thumbnail.cpp
)

target_compile_options(mpv-compose PRIVATE -Werror)
set_property(TARGET mpv-compose PROPERTY CXX_STANDARD 11)

target_link_libraries(mpv-compose
        log
        GLESv3
        EGL
        atomic
        swscale
        avcodec
        mpv
)

target_include_directories(mpv-compose PRIVATE "${PREFIX}/include")