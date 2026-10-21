# DayGrid

A minimal yearly visual accountability native Android application inspired by the life-calendar concept.

See your entire year at a glance as a grid of 365 (or 366) dots. Each evening, DayGrid asks you one simple question: **Did you make today count?**

---

## 📱 Features

- **Year at a Glance Grid:** Visual display of every calendar day in the year. Interactive cells reveal past notes, statistics, and milestones.
- **Home Screen Widgets:** Compact (2×2) and Expanded (4×3) Glance widgets for your Android home screen.
- **Dynamic Lock-Screen Wallpaper:** Generate custom wallpapers with your current year's grid rendered directly onto your lock screen.
- **Evening Reflection:** Daily reminder notification with quick action buttons ("Yes, Made it Count" / "Rest / Missed").
- **Private & Local-First:** All data stored locally on your device with Room DB. Export and import JSON backups anytime with zero cloud lock-in.
- **CMF Phone 2 Pro / Nothing OS Aesthetic:** Monochromatic dark palette with bold dot-matrix accents, high-contrast typography, and fluid micro-animations.

---

## 📥 How to Install on Your Mobile Device

### Method 1: Download Pre-built APK from GitHub Actions
1. Go to the [Actions tab](https://github.com/ganesh-code/DayGrid/actions) in this repository.
2. Click the latest workflow run.
3. Under **Artifacts**, download `DayGrid-debug-apk.zip`.
4. Extract the zip file and open `app-debug.apk` on your Android device (Android 13+).
5. Allow "Install from unknown sources" if prompted, and launch DayGrid!

### Method 2: Build Locally with Android Studio
1. Clone this repository:
   ```bash
   git clone https://github.com/ganesh-code/DayGrid.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync dependencies.
4. Connect your Android phone via USB with **USB Debugging enabled**.
5. Click **Run** or execute:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0
- **UI:** Jetpack Compose + Material 3
- **Widgets:** Jetpack Glance
- **Database:** Room DB (SQLite)
- **Dependency Injection:** Hilt
- **Background Tasks & Alarms:** Android AlarmManager + BroadcastReceiver
- **Architecture:** Clean Architecture (Presentation, Domain, Data)
- **Target SDK:** Android 15 (API 35), Min SDK: Android 13 (API 33)
