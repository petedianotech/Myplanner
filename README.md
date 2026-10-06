# Pete

Personal Android AI assistant — voice-first, offline data, Gemini-ready.

**Version 2.0.1-pete** · Kotlin · Jetpack Compose · Material 3 · Room

## Features
- Ambient home with gradients, glass cards, pill CTAs
- Conversation canvas + local command router
- Daily brief, focus timer, quick command
- Tasks, reminders, notes, ideas, voice notes (local)
- Splash + Pete adaptive icon
- Gemini API key via `BuildConfig` (optional until Live is fully wired)

## API key (Gemini)

**Never commit your key.**

### Local (Android Studio)
1. Create `local.properties` in the **project root** (same folder as `settings.gradle.kts`).
2. Add:
   ```properties
   GEMINI_API_KEY=your_google_ai_studio_key
   ```
3. Rebuild. Key is available as `BuildConfig.GEMINI_API_KEY` / `GeminiConfig.apiKey`.

### GitHub Actions (APK CI)
1. Repo → **Settings → Secrets and variables → Actions**
2. New secret name: **`GEMINI_API_KEY`**
3. Value: your Google AI Studio key  
4. Push to `main` — workflow injects the secret into the build.

Get a key: https://aistudio.google.com/apikey

## Build
```bash
./gradlew :app:assembleDebug
# APK → app/build/outputs/apk/debug/
```

CI artifact name: **pete-debug-apk**

## Privacy
Planner data stays on device. Gemini is only used when you configure a key and enable live AI.
