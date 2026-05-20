# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.yago.aegis.ExampleUnitTest"

# Clean build
./gradlew clean assembleDebug
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Architecture

**Single-module Android app** (`:app`) using MVVM + Jetpack Compose. No Hilt/DI framework — dependencies are wired manually in `MainActivity` and passed down as constructor arguments.

### Data flow

```
SettingsStore (DataStore/Preferences)
    ↓ exposes Flow<T>
UserRepository  ←→  FirestoreDataSource (Firestore)
    ↓ injects into
ViewModels (WorkoutViewModel, RoutinesViewModel, ProfileViewModel, StatsViewModel, AuthViewModel)
    ↓ StateFlow<T>
Screens (Composables)
```

**All persistent data lives in two places simultaneously:**
- **Local:** `SettingsStore` — DataStore Preferences, serializing complex types (Routines, Exercises, WorkoutHistory) as Gson JSON strings.
- **Cloud:** `FirestoreDataSource` — Firestore under `users/{uid}/data/{collection}`. On login, `UserRepository.syncOnLogin()` merges cloud → local (cloud wins if data exists; local is uploaded if no cloud data yet).

### ViewModels

- **`WorkoutViewModel`** — active session state, rest timer, set tracking, finish/save flow. Survives navigation (created in `MainActivity`).
- **`RoutinesViewModel`** — routine CRUD and exercise library. Also created in `MainActivity`.
- **`ProfileViewModel`** — user profile, onboarding state, body metrics.
- **`StatsViewModel`** — workout history, exercise analytics, progression charts. Created via `viewModel()` in `AegisNavigation` and shared across `StatsScreen` and `WorkoutCompleteScreen`.
- **`AuthViewModel`** — Firebase Auth (email/password + Google Sign-In), email verification flow. Created via `viewModel()` in `AegisNavigation`.

All ViewModels use `ViewModelProvider.Factory` pattern (manual, no Hilt).

### Navigation

`AegisNavigation.kt` owns the entire `NavHost`. Routes are plain strings (not the `Screen` sealed class, which is only used for the bottom bar tabs). Key flow:

- **Unauthenticated:** `welcome → identity → metrics → register → email_verification`
- **Authenticated:** bottom bar tabs (`profile`, `routine`, `train`, `ejercicios`, `stats`) + overlay screens
- **Active session:** `active_session/{routineId}` hides the bottom bar; session state lives in `WorkoutViewModel` so the user can navigate away (pause) and return

Bottom bar visibility is computed explicitly by excluding specific routes in `AegisNavigation`.

### Data models (core)

- `Exercise` — exercise definition with `id: Long` (UUID bits), tags, icon, `lastPerformance`, `oneRepMax`, `bestSet`, `history: List<ExerciseRecord>`
- `Routine` — ordered list of `Exercise`, with `iconName` and `lastCompletedDates`
- `WorkoutSession` — a completed training session: `id: String` (UUID), `routineName`, `date`, `exercisesProgress: List<ExerciseProgress>`, `notes`
- `ExerciseProgress` — exercise + its sets during a session
- `ExerciseSet` — individual set: `id`, `reps`, `weight`, `isCompleted`

## UI / Theme

Dark luxury aesthetic: near-black backgrounds (`BackgroundBlack #050505`, `SurfaceDark #0E0E0E`) with bronze/gold accents (`AegisBronze #B39371`, `AegisGoldAccent #D4AF37`). All colors defined in `ui/theme/Color.kt`.

Components prefixed with `Aegis` (e.g., `AegisAlertDialog`, `AegisBottomBar`, `AegisTopBar`) are reusable design-system pieces. Use them instead of raw Material3 equivalents to stay consistent.

## Firebase setup

The project uses Firebase Auth + Firestore + Analytics via `google-services.json` (not committed). A `google-services.json` file must be present at `app/google-services.json` for the build to succeed.

Firestore structure per user: `users/{uid}/data/{profile|routines|exercises|history|tags|settings}` — each document stores its payload as a Gson-serialized JSON string in a `"data"` field plus an `"updatedAt"` timestamp.

## Contexto adicional del proyecto

### Decisiones de arquitectura tomadas
- isLoggedIn es StateFlow<Boolean> en AuthViewModel para navegación reactiva
- Ejercicios base tienen BASE_TAG = "__base__" y sufijo Zero Width Space en el nombre
- imePadding() aplicado en NavHost raíz para teclado en toda la app
- WindowCompat.setDecorFitsSystemWindows(window, false) en MainActivity
- DataStore se limpia completamente al hacer logout via SettingsStore.clearAll()
- Verificación de email obligatoria antes de entrar a la app

### Convenciones críticas
- NUNCA usar LiveData, solo StateFlow
- Nuevos campos en data classes SIEMPRE con default value para retrocompatibilidad Gson
- Colores siempre via MaterialTheme, nunca hardcodeados
- IDs de ejercicios: UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE

### Bugs resueltos — no revertir
- Datos de sesión se perdían al ir a WorkoutSettings → fix en startWorkout()
- Keys duplicadas en LazyColumn historial → key compuesta con id+index
- % ganancia calculaba mal → ahora compara máximo por sesión
- Splash screen blanca → themes.xml con windowBackground #0A0A0A

### Backlog pendiente (ordenado por prioridad)
1. Confirmación al borrar rutinas
2. Estado vacío en Profile con guía para nuevos usuarios
3. versionCode/versionName en build.gradle.kts
4. Política de privacidad URL pública para Play Store
5. Mapa muscular heatmap coloreado por intensidad mensual
6. IA entrenador personal integrado con Claude API
7. Unificar design system
8. Mejorar flow de crear rutina
9. Calculadora de platos
10. Compartir entrenamiento como imagen