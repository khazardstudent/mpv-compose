# Mpv Compose

Mpv component for jetpack compose based on [mpv-android](https://github.com/mpv-android/mpv-android)

![Maven Central Version](https://img.shields.io/maven-central/v/dev.marcelsoftware.mpvcompose/mpv-compose)

## Installation

Add mpv-compose as an dependency

```kotlin
implementation("dev.marcelsoftware.mpvcompose:mpv-compose:1.0.0")
```

## Usage

See [sample implementation](app/src/main/java/dev/marcelsoftware/mpvcomposesample/MainActivity.kt) for a complete example.

## Building

Mpv Compose requires first some native libraries to be built. This is done by downloading dependencies, and building the required libraries.

### Cd to the `buildscripts` directory
```sh
cd buildscripts
```

### Download dependencies

`download.sh` will take care of installing the Android SDK, NDK and downloading the sources.

If you're running on Debian/Ubuntu or RHEL/Fedora it will also install the necessary dependencies for you.
```sh
./download.sh
```

`buildall.sh` will take care of building the required libraries for the supported arches.

```sh
./buildall.sh
```
> Run buildall.sh with --clean to clean the build directories before building. For a guaranteed clean build also run rm -rf prefix beforehand.

## License

Licensed under `MIT`. For the `mpv-android` license see [mpv-compose/LICENSE](mpv-compose/LICENSE).