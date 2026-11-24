# Branch: feature/core-architecture

This branch builds the technical foundation that every subsequent feature branch depends on. No business feature is implemented here — the goal is a compilable, runnable application skeleton with all infrastructure wired up.

---

## What you will learn

- How to set up a multi-module Android project with Gradle Kotlin DSL and a version catalog
- How to wire Hilt for application-wide dependency injection
- How to configure Retrofit and OkHttp for a REST API client, including request logging
- How to intercept HTTP requests to attach a Bearer token automatically
- How to handle token expiry transparently with an `Authenticator`
- How to store credentials securely using Android Keystore-backed encryption
- How to define a shared Room database that all feature modules will use
- How to build a navigation graph that separates the auth flow from the main app flow
- How to apply a Material 3 theme with light and dark mode support

---

## What it delivers

### Multi-module Gradle setup

The project is structured as a multi-module build from the start. `settings.gradle.kts` lists every module. Each module has its own `build.gradle.kts` that declares only what it actually needs.

A central version catalog (`gradle/libs.versions.toml`) pins every dependency version in one file. No module hardcodes a version string — they all reference the catalog. This prevents version conflicts and makes upgrades trivial.

### Hilt application setup

`TutorialApplication` is annotated with `@HiltAndroidApp`. This triggers Hilt's code generation and creates the application-level component. Every module that uses `@Inject` depends on this setup.

`DispatchersModule` provides three named `CoroutineDispatcher` instances: `@IoDispatcher` (for network and database), `@MainDispatcher` (for UI updates) and `@DefaultDispatcher` (for CPU-bound computation). Injecting dispatchers instead of using `Dispatchers.IO` directly makes code testable — tests can substitute a `TestCoroutineDispatcher`.

### Network stack

`NetworkModule` wires three objects:

1. A primary `OkHttpClient` with `AuthInterceptor` and `TokenAuthenticator`. All authenticated requests go through this client.
2. A secondary `@AuthOnly OkHttpClient` with `AuthInterceptor` only — no `TokenAuthenticator`. This is the client used by `TokenAuthenticator` itself when it needs to call the refresh endpoint. Without this secondary client, calling the refresh endpoint would trigger another 401 handling cycle, causing an infinite loop.
3. A `Retrofit` instance built on top of the primary client, with the Kotlinx Serialization converter.

`AuthInterceptor` reads the stored `access_token` on every request and injects the `Authorization: Bearer <token>` header. It is an `Interceptor`, not an `Authenticator` — it runs before the request, not after a failure.

`TokenAuthenticator` runs after a 401 response. It calls the refresh endpoint, saves the new tokens and returns the original request with the updated `Authorization` header. A `Mutex` serializes concurrent retries: if three requests fail with 401 at the same time, only one refresh call is made; the other two wait and use the new token when the mutex is released.

### Secure storage

`SecureTokenStorage` stores the `access_token` and `refresh_token` using `EncryptedSharedPreferences` backed by Android Keystore. The encryption key is generated once and stored in the hardware-secured key store — it never leaves the device and cannot be extracted. Plain `SharedPreferences` or `DataStore` are intentionally not used for tokens.

`DataStoreModule` provides a `DataStore<Preferences>` instance for non-sensitive preferences (user preferences, cached flags). It is distinct from token storage.

### Room database

`TutorialDatabase` is a shared Room database defined in `:core:database`. Feature modules add their entities here as the project grows. Version 1 starts with zero entities — the schema grows with each feature branch.

`DatabaseModule` provides a singleton `TutorialDatabase` instance and all DAOs. Because Room uses a single SQLite connection shared across modules, the database must be centralized.

### Navigation

The navigation graph is split into two nested graphs: `authGraph` (sign-in, register, OTP flow) and `mainGraph` (home, cart, orders, profile). The app decides on launch which graph to start in by checking whether a stored token exists.

`Screen` is a sealed class that defines every route as a typed object. Route strings are defined once; no magic strings are scattered across composables.

### Material 3 theme

`TutorialTheme` wraps `MaterialTheme` with a custom color scheme defined in `Color.kt`. The light and dark schemes use Material 3 role-based tokens, not hardcoded hex values. All composables in the app are wrapped in `TutorialTheme` via `MainActivity.setContent { TutorialTheme { ... } }`.

### Debug vs release build types

The `debug` build type enables cleartext HTTP via `network_security_config.xml`. This allows the app to reach the backend at `http://10.0.2.2:8080` during development without HTTPS. The `release` build type enforces HTTPS only.

`BASE_URL` is injected from `BuildConfig` so the same code can target different environments without changes.

---

## Key files

| File | Module | Role |
|---|---|---|
| `settings.gradle.kts` | root | Declares all modules |
| `gradle/libs.versions.toml` | root | Version catalog |
| `TutorialApplication.kt` | `:app` | `@HiltAndroidApp` entry point |
| `MainActivity.kt` | `:app` | Compose entry point, `TutorialTheme` |
| `NetworkModule.kt` | `:app` | OkHttp, Retrofit, qualifiers |
| `AuthInterceptor.kt` | `:core:network` | Attaches Bearer token |
| `TokenAuthenticator.kt` | `:core:network` | Handles 401, refreshes token |
| `SecureTokenStorage.kt` | `:core:storage` | Keystore-backed token store |
| `TutorialDatabase.kt` | `:core:database` | Room database, schema version |
| `DatabaseModule.kt` | `:core:database` | Provides DB and DAOs |
| `AppNavigation.kt` | `:app` | NavHost, auth/main graph split |
| `Screen.kt` | `:app` | Typed route definitions |
| `TutorialTheme.kt` | `:core:ui` | Material 3 theme wrapper |
| `Color.kt` | `:core:ui` | Light and dark color schemes |
