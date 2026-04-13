# LG AC Remote Control App

## Project Overview
This project is a complete, production-ready Android application designed specifically to control an LG Dual Inverter Air Conditioner using the built-in infrared (IR) blaster of a smartphone (such as the Huawei Mate XT). 

The application is built using modern Android development practices, featuring a user interface constructed with **Jetpack Compose** and written entirely in **Kotlin**. It leverages the Android `ConsumerIrManager` API to transmit precise 28-bit IR protocols (LG2 format) required by LG Dual Inverter models.

**Key Technologies:**
- **Language:** Kotlin (JVM 1.8)
- **UI Framework:** Jetpack Compose (Material 3)
- **Build System:** Gradle (Version 8+)
- **Hardware API:** `android.hardware.consumerir` (`ConsumerIrManager`)

**Architecture:**
- `MainActivity.kt`: Contains the Jetpack Compose UI layout (temperature display, power, fan, mode controls) and handles the click events to trigger the IR transmission.
- `LgIrCodeGenerator.kt`: Contains the core logic for calculating the LG AC 28-bit protocol payload. It handles the checksum generation and translates the desired state (power, temperature, mode, fan speed) into an array of microseconds representing the carrier frequency mark and space timings.

## Building and Running

The project uses the standard Android Gradle Wrapper for building and assembling the application. 

**Requirements:**
- Java Development Kit (JDK) 17+ (Project targets source/target compatibility 1.8 but Gradle 8.4+ requires JDK 17+)
- Android SDK (compileSdk 34, minSdk 24)

**Commands:**

*   **Build the Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
    *The generated APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`*

*   **Install on a Connected Device:**
    ```bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```

*   **Launch the App via ADB:**
    ```bash
    adb shell am start -n com.example.lgacremote/.MainActivity
    ```

*   **Clean the Build:**
    ```bash
    ./gradlew clean
    ```

## Development Conventions

*   **UI Declarations:** All UI components are declared using Jetpack Compose within `MainActivity.kt`. Styling follows Material 3 guidelines and uses a dark color scheme by default.
*   **State Management:** The app utilizes Compose's `remember { mutableStateOf(...) }` to manage the localized state of the remote control (e.g., current temperature, selected mode, fan speed).
*   **Hardware Abstraction:** The generation of the raw IR timings is isolated within the `LgIrCodeGenerator` object. This separation ensures that the UI layer only deals with user actions and state, while the object handles the bitwise operations and array formatting required by the `ConsumerIrManager`.
*   **Permissions:** The app requests the `android.permission.TRANSMIT_IR` permission in the `AndroidManifest.xml`, which is a normal permission granted automatically at install time. It also uses `<uses-feature android:name="android.hardware.consumerir" android:required="false" />` to indicate IR support without strictly preventing installation on non-IR devices (though the app will show a Toast message if the emitter is missing).
