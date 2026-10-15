# petediano

Personal Android AI assistant — Gemini chat with **web search**, Live voice, habits, and offline planner.

**Version 2.5.0-chat**

## Chat (production)
- Modern bubble UI (ChatGPT / Grok style)
- **Real Gemini answers** (not local stubs) for general questions
- **Google Search grounding** — toggle in chat header
- **Image generation** via Nano Banana (`draw a…`) — requires billed API key
- Free-tier models: 3.5 / 3.1 Flash-Lite, 3.8 / 3.6 / 2.5 Flash
- Model picker, citations, typing indicator, suggestion chips

## Other features
- Gemini Live voice (`gemini-3.8-live`)
- Habits streaks, JSON backup export
- Exact-time reminders + missed recovery
- Floating overlay bubble

## API key
`local.properties`:
```
GEMINI_API_KEY=your_key
```
GitHub Actions secret: **GEMINI_API_KEY**

Image models need a **billed** Google AI project. Text + search work on free tier.

## Build
```
./gradlew :app:assembleDebug
```
CI artifact: **pete-debug-apk**
