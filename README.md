# MyPlanner

Native Android productivity planner — offline-first, Kotlin, Jetpack Compose, Material 3.

## Hello & first-run setup

- **Hello screen**: identity, “Get started” / “Explore first”, offline privacy note
- **Notification setup**: explains local reminders, requests `POST_NOTIFICATIONS` on Android 13+
- **Optional first reminder**: create or skip; saves to Room when created
- **Home**: calm landing after setup
- DataStore remembers completion so Hello does not reappear

### Architecture

- Room: `ReminderEntity` + DAO (local source of truth)
- DataStore: onboarding, notification flags
- Navigation Compose: Hello → notification → first reminder → home

### Build

Open in Android Studio and run. minSdk 26, targetSdk 35.
