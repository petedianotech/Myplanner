# Pete

Personal Android AI assistant — Gemini-powered chat, voice, and overlay bubble.

**Version 2.1.0-pete**

## Features
- Ambient home (gradients, glass, pills)
- In-app conversation: **type or speak**
- Gemini live chat when `GEMINI_API_KEY` is set
- Local command router (tasks, reminders, focus) offline
- Floating **bubble** over other apps (type + mic)
- Daily brief, focus timer, quick command
- Splash + Pete icon

## API key
### Local
`local.properties` in project root:
```
GEMINI_API_KEY=your_key
```

### GitHub Actions
Secret name: **GEMINI_API_KEY**

## Overlay bubble
Settings → Assistant → Floating bubble  
Grant **Display over other apps**, then enable.

## Build
```
./gradlew :app:assembleDebug
```
CI artifact: **pete-debug-apk**
