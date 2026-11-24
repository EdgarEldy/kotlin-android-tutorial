# Setting up the development environment

This guide installs all tools required to build, run and test the Android application on Windows. No prior Android experience is assumed.

---

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java | 17 | Reuse the existing JDK from `spring-boot-tutorial` — do not install a second one |
| IntelliJ IDEA Ultimate | Latest | Android plugin must be enabled |
| Android SDK | API 36 | Installed via `sdkmanager`, not through Android Studio |
| Git | Any recent | Required for the branching workflow |

> **Why IntelliJ IDEA and not Android Studio?**
> IntelliJ IDEA Ultimate includes the full Android plugin and also supports the Spring Boot backend in the same IDE instance. Android Studio is a good alternative if you work on mobile only.

---

## 1. Install the Android SDK

Android Studio ships the SDK automatically. Since we use IntelliJ IDEA, we install the SDK separately via the command-line tools.

### Download the command-line tools

1. Go to [developer.android.com/studio#command-line-tools-only](https://developer.android.com/studio#command-line-tools-only) and download the Windows zip.
2. Extract to `C:\Users\<you>\AppData\Local\Android\Sdk\cmdline-tools\latest\`.
3. The resulting directory must contain `bin\sdkmanager.bat` directly under `latest\`.

### Install required SDK packages

Open PowerShell and run:

```powershell
$env:ANDROID_HOME = "$env:USERPROFILE\AppData\Local\Android\Sdk"
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.1.0"
sdkmanager --licenses
```

Accept all license agreements. The download takes a few minutes.

### Set environment variables permanently

In Windows, open **System Properties → Environment Variables** and add:

| Variable | Value |
|---|---|
| `ANDROID_HOME` | `C:\Users\<you>\AppData\Local\Android\Sdk` |
| Append to `PATH` | `%ANDROID_HOME%\platform-tools` |
| Append to `PATH` | `%ANDROID_HOME%\cmdline-tools\latest\bin` |

Restart PowerShell after saving. Verify:

```powershell
adb version
sdkmanager --version
```

---

## 2. Configure IntelliJ IDEA

1. Install the **Android** and **Kotlin** plugins from the plugin marketplace if not already present (**Settings → Plugins**).
2. Set the project SDK to your existing Java 17 installation: **File → Project Structure → Project → SDK**.
3. Point IntelliJ to the Android SDK: **Settings → Languages & Frameworks → Android SDK → Android SDK Location** → `C:\Users\<you>\AppData\Local\Android\Sdk`.
4. Open the project root as a Gradle project.
5. Wait for the initial Gradle sync to complete. The first sync downloads all dependencies and may take several minutes.

---

## 3. Create an Android Virtual Device (emulator)

A virtual device lets you run the application without a physical phone.

```powershell
# Download a system image for API 36
sdkmanager "system-images;android-36;google_apis;x86_64"

# Create a virtual device named Pixel9
avdmanager create avd -n Pixel9 -k "system-images;android-36;google_apis;x86_64"

# Start the emulator
emulator -avd Pixel9
```

The emulator is ready when the Android home screen appears.

> **Reaching the backend from the emulator:** The Android emulator runs in an isolated network. `localhost` resolves to the emulator itself, not your machine. Use `10.0.2.2` to reach a server running on your host (e.g., `http://10.0.2.2:8080` for the Spring Boot backend).

---

## 4. Connect a physical device (optional)

A physical device provides better performance for real-world testing.

1. On the device: open **Settings → About phone**, tap **Build number** 7 times to enable Developer options.
2. Open **Developer options** and enable **USB debugging**.
3. Connect the device via USB and confirm the authorization prompt on the device screen.

Verify the device is recognized:

```powershell
adb devices
```

You should see your device listed with status `device`.

---

## 5. Build and install the application

```powershell
# From the project root
.\gradlew app:assembleDebug
.\gradlew app:installDebug
```

> `assembleDebug` compiles the APK. `installDebug` additionally pushes it to the connected device or emulator.

The application requires the backend to be running. Start `spring-boot-tutorial` before launching the app.

---

## 6. Run the tests

Unit tests run on the JVM — no device needed:

```powershell
.\gradlew test
```

Instrumented tests run on a connected device or emulator:

```powershell
.\gradlew connectedDebugAndroidTest
```

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `ANDROID_HOME` not found during Gradle sync | Set the environment variable and restart IntelliJ |
| `adb: command not found` | Add `%ANDROID_HOME%\platform-tools` to `PATH` |
| Emulator starts but stays black | Wait 60 seconds; cold boot takes time on the first launch |
| `Connection refused` when the app calls the API | Check the backend is running and use `10.0.2.2`, not `localhost` |
| `adb devices` shows `unauthorized` | Reconnect USB; confirm the prompt on the device |
