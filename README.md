# MyPlanner

Native Android productivity planner — offline-first, built with Kotlin, Jetpack Compose, and Material 3.

## Phase 1 — Foundation

This release establishes the production-quality project structure and design system only. No feature screens (tasks, reminders, onboarding) are implemented yet.

### What’s included

- **Stack**: Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room, DataStore, WorkManager (foundation only)
- **Architecture**: Clear UI / data separation, offline-first (Room as future source of truth, DataStore for preferences)
- **Design system**: Custom Material 3 light & dark themes (indigo primary + teal accent), typography, shapes, spacing, reusable components
- **Validation screen**: Temporary foundation screen that exercises theme, typography, colors, buttons, cards, and empty state
- **Responsive**: Edge-to-edge, safe drawing insets, adaptive layout foundations
- **Build**: R8 ready for release, minSdk 26, targetSdk 35

### Requirements

- Android Studio Ladybug or newer (or equivalent)
- JDK 17+
- Android SDK 35

### Build & run

```bash
./gradlew assembleDebug
# or open in Android Studio and Run
```

### Package structure

```
com.myplanner.app
├── data
│   ├── local          # Room database, DataStore preferences
│   └── repository     # (future)
├── ui
│   ├── theme          # Color, Type, Shape, Spacing, Theme
│   ├── components     # AppButton, AppCard, SectionHeader, EmptyState
│   ├── navigation     # NavGraph foundation
│   └── foundation     # Phase 1 validation screen
├── MainActivity.kt
└── MyPlannerApplication.kt
```

### Design tokens (starting palette)

| Role            | Light        | Dark         |
|-----------------|--------------|--------------|
| Primary         | `#4F46E5`    | `#A5B4FC`    |
| Secondary       | `#14B8A6`    | `#5EEAD4`    |
| Background      | `#F8F9FC`    | `#0D0F14`    |
| Surface         | `#FFFFFF`    | `#171A21`    |
| On surface      | `#171923`    | `#E8EAED`    |
| On surface var. | `#687083`    | `#9AA3B2`    |

### Next phases (not in this branch)

- Onboarding / Hello screen
- Task & plan models in Room
- Local reminder scheduling (AlarmManager)
- Settings with theme preference
- Lists, detail screens, and navigation destinations

### License

Private / proprietary unless otherwise stated.
