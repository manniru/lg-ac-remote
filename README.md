# LG AC Remote Control App

## Project Overview
This project is a complete, production-ready Android application designed specifically to control an LG Dual Inverter Air Conditioner using the built-in infrared (IR) blaster of a smartphone (such as the Huawei Mate XT). 

The application is built using modern Android development practices, featuring a user interface constructed with **Jetpack Compose** and written entirely in **Kotlin**. It leverages the Android `ConsumerIrManager` API to transmit precise 28-bit IR protocols (LG2 format) required by LG Dual Inverter models.

**Key Technologies:**
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Build System:** Gradle
- **Hardware API:** `android.hardware.consumerir` (`ConsumerIrManager`)

## Features
- **Power Toggle:** Turn the AC on and off.
- **Temperature Control:** Adjust the target temperature between 16°C and 30°C.
- **Mode Selection:** Switch between Cool, Dry, Fan, and Heat modes.
- **Fan Speed:** Adjust fan speed between Low, Mid, High, and Auto.

## Building and Running

**Requirements:**
- Java Development Kit (JDK) 17+
- Android SDK (compileSdk 34, minSdk 24)

**Commands:**

*   **Build the Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```

*   **Install on a Connected Device (via ADB):**
    ```bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```

## Notes
Ensure your Android device has a built-in IR blaster. If the device does not have IR hardware, the app will show a Toast message indicating that the IR emitter was not found.
