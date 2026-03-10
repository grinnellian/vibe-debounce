# Debounce — Notification Vibration Debouncer for Android

## Project Overview
Android app that debounces notification vibrations from rapid-fire messaging. First message vibrates normally; subsequent messages from the same sender within a configurable window are silenced. All notifications remain visible in the shade.

## Framework
This project uses the [Ainulindale](https://github.com/grinnellian/ai-lindale) multi-agent framework via git submodule at `.ai-lindale/`. Core agents (architect, tpm, dev) are symlinked into `.claude/`.

## Tech Stack
- **Language:** Kotlin
- **Target SDK:** 35 (Android 15)
- **Min SDK:** 31 (Android 12)
- **Build:** Gradle with Kotlin DSL (`build.gradle.kts`)
- **UI:** ViewBinding with Material 3
- **Dependencies:** Minimal — AndroidX core, AppCompat, Material, ConstraintLayout

## Key Architecture
- `DebounceNotificationService` — NotificationListenerService that intercepts notifications
- `RingerStateManager` — thread-safe ringer state capture/restore with reference counting
- `DebounceTimer` — Handler-based cancellable/restartable timer
- `SenderKey` — data class (packageName + senderName) used as map key
- `MainActivity` — single settings screen with debounce slider and permission grants

## Permissions Required
- `BIND_NOTIFICATION_LISTENER_SERVICE` — read incoming notifications
- `ACCESS_NOTIFICATION_POLICY` — change ringer mode (DND access)
- `VIBRATE` — trigger vibration for new-thread alerts

## Build & Test
```bash
./gradlew assembleDebug    # build debug APK
./gradlew test             # run unit tests
./gradlew lint             # run lint checks
```

## Project Structure
```
app/src/main/java/com/vibedebounce/
├── model/
│   ├── SenderKey.kt
│   └── DebounceTimer.kt
├── service/
│   ├── DebounceNotificationService.kt
│   └── RingerStateManager.kt
└── ui/
    └── MainActivity.kt
```

## Key Invariant
**Always restore the ringer state that existed before muting.** If the user was already on silent, leave them on silent when the debounce window expires.

## Distribution
Sideload only. No Play Store. Signed APK is the target artifact.
