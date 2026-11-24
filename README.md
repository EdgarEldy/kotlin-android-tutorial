# Kotlin Android Tutorial

A complete, hands-on walkthrough of building a production-quality native Android e-commerce application using **Kotlin, Jetpack Compose and Material 3**, connected to the [`spring-boot-tutorial`](https://github.com/EdgarEldy/spring-boot-tutorial) backend.

The application covers the full customer journey: account creation, authentication, product browsing, cart management, checkout and order tracking. It is both a realistic API client and a structured learning resource for modern Android development.

---

## Table of contents

- [Application features](#application-features)
- [Screens](#screens)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Package structure](#package-structure)
- [Backend integration](#backend-integration)
- [Security](#security)
- [Development environment](#development-environment)
- [Running on a device or emulator](#running-on-a-device-or-emulator)
- [Build and test](#build-and-test)
- [Branching strategy](#branching-strategy)
- [feature/core-architecture](#featurecore-architecture)
- [feature/auth](#featureauth)
- [feature/product-catalog](#featureproduct-catalog)
- [feature/shopping-cart](#featureshopping-cart)
- [feature/orders](#featureorders)
- [Implementation status](#implementation-status)
- [Concepts covered](#concepts-covered)

---

## Application features

**Authentication**
- Sign in with email and password
- Create an account (registration + 6-digit activation code sent by email)
- Restore a session automatically on app launch
- Reset a forgotten password using a 6-digit OTP sent by email
- Sign out and clear all local credentials

**Product discovery**
- Browse the home screen with featured products and categories
- Navigate product categories
- Search and filter products by name, category and price range
- Browse paginated product listings
- View product details with images, description and price
- Browse products and categories while offline (served from local cache)

**Shopping**
- Add products to the server-side cart
- Update quantities or remove items
- View server-calculated cart totals
- Place an order from the cart in one tap
- Receive order confirmation

**Orders and account**
- Browse order history with status tracking
- View full order details and item breakdown
- Display profile information
- Sign out

---

## Screens

| Screen | Description |
|---|---|
| Sign in | Email + password form with form validation |
| Register | Multi-field registration form; password strength rules enforced by the backend |
| Activate account | 6-digit OTP entry after registration |
| Forgot password | Email entry to request a reset OTP |
| Verify OTP | 6-digit OTP entry to validate before the new password screen |
| Reset password | New password + confirmation; submits OTP to the backend |
| Home | Featured products, category chips, quick navigation |
| Categories | Paginated category list |
| Product list | Filterable, searchable, paginated product grid |
| Product detail | Images, description, price, quantity picker, add-to-cart |
| Cart | Line items, quantities, subtotals, server-calculated total, checkout button |
| Checkout confirmation | Order summary and confirmed order number |
| Order history | Paginated list of past orders with status badges |
| Order detail | Full order with item breakdown and total |
| Account | Profile information and sign out |

---

## Tech stack

### Application

| Component | Choice |
|---|---|
| Language | Kotlin |
| UI framework | Jetpack Compose |
| Design system | Material 3 |
| Navigation | Navigation Compose |
| Image loading | Coil |

### Architecture

| Component | Choice |
|---|---|
| Pattern | MVVM + pragmatic Clean Architecture |
| Dependency injection | Hilt |
| Async | Kotlin Coroutines + Flow |
| State management | `StateFlow`, immutable UI state |

### Data and networking

| Component | Choice |
|---|---|
| HTTP client | Retrofit + OkHttp |
| Serialization | Kotlin Serialization |
| Secure storage | Android Keystore-backed encryption |
| Preferences | DataStore |
| Local cache | Room (SQLite) |
| API client | Generated from OpenAPI spec |

### Build and tooling

| Component | Choice |
|---|---|
| Build system | Gradle Wrapper + Kotlin DSL |
| Version catalog | `gradle/libs.versions.toml` |
| IDE | IntelliJ IDEA Ultimate (with Android plugin) |
| Java | Java 17 |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 36 (Android 16) |

### Testing

| Tool | Scope |
|---|---|
| JUnit | Unit tests |
| MockK | Kotlin mocking |
| Turbine | `StateFlow` and `Flow` testing |
| Compose UI Test | UI and navigation tests |
| MockWebServer | API integration tests |

---

## Architecture

The application follows MVVM with pragmatic Clean Architecture inside a multi-module Gradle project. Core infrastructure lives in `:core:*` modules; each feature is a self-contained `:feature:*` module with its own data, domain and presentation layers. Dependencies only point inward.

```
┌──────────────────────────────────────────────────────┐
│                  :feature:*                           │
│  presentation/ (screens, ViewModels, components)      │
│       calls domain/usecase/                           │
│  domain/ (models, repository interfaces, use cases)   │
│       implemented by data/                            │
│  data/ (DTOs, Retrofit services, repository impls)    │
└───────────────────────┬──────────────────────────────┘
                        │ depends on
┌───────────────────────▼──────────────────────────────┐
│  :core:network · :core:storage · :core:database       │
│  :core:ui · :app (NetworkModule, navigation)          │
└───────────────────────┬──────────────────────────────┘
                        │
              ┌─────────▼─────────┐
              │  Spring Boot API  │
              └───────────────────┘
```

### Layer responsibilities

| Layer | Contents |
|---|---|
| **presentation/** | Compose screens, reusable composables, ViewModels, immutable `UiState` |
| **domain/** | Pure models, repository interfaces, use cases (all business logic lives here) |
| **data/** | Repository implementations, Retrofit DTOs, DTO-to-domain mappers |
| **:core:network** | OkHttp / Retrofit wiring, `AuthInterceptor`, `TokenAuthenticator` |
| **:core:storage** | Keystore-backed `SecureTokenStorage`, DataStore |
| **:core:database** | Shared `TutorialDatabase` (Room) |
| **:core:ui** | `TutorialTheme`, color tokens, typography |

### Key rules

- Composables render state and emit events; they never call Retrofit directly.
- ViewModels expose `StateFlow<UiState>` as the single source of truth for each screen.
- DTOs never leak out of `data/` — ViewModels and composables only see domain models.
- Backend business rules (prices, cart totals, order status) are never recalculated on the client.

---

## Module structure

The project uses a multi-module Gradle layout. Each feature is a self-contained library module that owns its data, domain and presentation layers.

```
:app                      ← entry point, MainActivity, TutorialApplication, navigation, NetworkModule
:core:network             ← OkHttp, Retrofit wiring, AuthInterceptor, TokenAuthenticator
:core:storage             ← Keystore-backed SecureTokenStorage, DataStore module
:core:database            ← shared TutorialDatabase (Room), DatabaseModule
:core:ui                  ← Material 3 theme: TutorialTheme, Color, Type
:feature:auth             ← sign-in, register, OTP, password reset, profile
:feature:product-catalog  ← home, categories, product list, product detail
:feature:cart             ← cart, checkout, order confirmation
:feature:orders           ← order history, order detail
```

### Internal structure of a feature module

Each `:feature:*` module follows the same layout:

```
feature/<name>/src/main/java/com/edgareldy/tutorial/<name>/
├── data/
│   ├── dto/           ← Retrofit DTOs (@Serializable, @SerialName snake_case)
│   ├── api/           ← Retrofit service interfaces
│   └── repository/    ← Repository implementations
├── domain/
│   ├── model/         ← Pure domain models (no Retrofit or Room dependency)
│   ├── repository/    ← Repository interfaces
│   └── usecase/       ← One file per use case
└── presentation/
    ├── screens/       ← Compose screens
    ├── viewmodel/     ← ViewModels + sealed UiState
    └── components/    ← Composables reusable within this feature
```

---

## Backend integration

The application is a pure API client. All business logic, authentication, prices, cart ownership and order management live exclusively in the backend.

| Item | Value |
|---|---|
| Backend | `spring-boot-tutorial` (Spring Boot 3.5.x) |
| API prefix | `/api/v1` |
| Auth | JWT Bearer (`access_token` + `refresh_token`) |
| OpenAPI snapshot | `openapi/openapi.json` |

### Network targets by environment

| Build variant | Base URL |
|---|---|
| Debug (emulator) | `http://10.0.2.2:8080` |
| Debug (physical device) | `http://<host-lan-ip>:8080` |
| Staging / production | HTTPS URL supplied per environment |

> Inside the standard Android emulator, `localhost` resolves to the emulator itself. Use `10.0.2.2` to reach the host machine where the backend is running.

Cleartext HTTP is allowed in debug builds only. Release builds require HTTPS.

### Response envelope

Every API response is wrapped in a consistent envelope:

```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": { ... },
  "timestamp": "2026-11-21T10:15:30Z"
}
```

Paginated endpoints return a `PageResponse<T>` in `data`:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

Error responses carry an RFC 7807 `ProblemDetail` in `data` with `success: false`.
All JSON field names use **snake_case** (`product_name`, `unit_price`, `access_token`).

### Authentication flows

**Sign in / session lifecycle**

```
POST /api/v1/auth/login          → { access_token, refresh_token, expires_in }
POST /api/v1/auth/refresh-token  → { access_token, refresh_token, expires_in }
POST /api/v1/auth/logout
GET  /api/v1/auth/me             → user profile
```

**Registration + activation**

```
POST /api/v1/auth/register          → account created (enabled = false)
POST /api/v1/auth/activate-account  → { email, activation_code (6 digits) }
```

**Mobile password reset (3-step OTP)**

```
POST /api/v1/password-reset/mobile      → { email }             # OTP sent by email
POST /api/v1/password-reset/verify-otp  → { email, otp }        # validates without consuming
POST /api/v1/password-reset/confirm     → ?token={otp}
                                           { new_password,
                                             confirm_password }  # resets and consumes OTP
```

> The web password reset endpoints at `/api/v1/auth/forgot-password` and `/api/v1/auth/reset-password` belong to the Angular frontend and are **never called** from this application.

### Mobile API surface

The table below lists only the endpoints consumed by the Android application. Admin-only endpoints (`/api/v1/users`, `/api/v1/customers`, status updates) are out of scope.

| Resource | Endpoint | Auth required |
|---|---|---|
| Products | `GET /api/v1/products` | No |
| Product detail | `GET /api/v1/products/{id}` | No |
| Categories | `GET /api/v1/categories` | No |
| Category detail | `GET /api/v1/categories/{id}` | No |
| Cart | `GET /api/v1/cart` | Yes |
| Add to cart | `POST /api/v1/cart/items` | Yes |
| Update cart item | `PATCH /api/v1/cart/items/{itemId}` | Yes |
| Remove cart item | `DELETE /api/v1/cart/items/{itemId}` | Yes |
| Clear cart | `DELETE /api/v1/cart/items` | Yes |
| Checkout | `POST /api/v1/orders` | Yes |
| Order history | `GET /api/v1/orders` | Yes |
| Order detail | `GET /api/v1/orders/{id}` | Yes |

---

## Security

- **Tokens** (`access_token`, `refresh_token`) are stored exclusively using Android Keystore-backed encryption. They are never written to SharedPreferences, DataStore, files or logs.
- **Passwords** are never stored or cached at any point.
- **OTP values** are held only in transient UI state; they are never persisted or logged.
- **Authorization headers** are attached centrally by an OkHttp interceptor; no screen manages tokens directly.
- **Token refresh** is handled transparently by an authenticator; concurrent refresh requests are serialized to avoid race conditions.
- **Logout** clears all stored credentials and invalidates the token server-side.
- **Cleartext HTTP** is restricted to development builds via `network_security_config.xml`.

---

## Development environment

The project is developed with **IntelliJ IDEA Ultimate** and the Android plugin.

### Requirements

| Requirement | Notes |
|---|---|
| Java 17 | Reuse the existing installation; do not install a second JDK |
| Android SDK | Install via `sdkmanager`; set `ANDROID_HOME` |
| Platform | `platforms;android-36` |
| Build tools | `build-tools;36.1.0` |
| Platform tools | `platform-tools` (`adb`) |
| IntelliJ IDEA Ultimate | Android and Kotlin plugins must be enabled |
| Git | Any recent version |

### Android SDK setup (command line)

```bash
# Install required packages
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.1.0"

# Accept licenses
sdkmanager --licenses

# Verify adb is on the path
adb version
```

### Environment variables

Add to your shell profile (`.bashrc`, `.zshrc`, or Windows user environment):

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
```

### IntelliJ IDEA setup

1. Open the plugin marketplace and ensure the **Android** and **Kotlin** plugins are installed and enabled.
2. Set the project JDK to the existing Java 17 installation (**File → Project Structure → SDK**).
3. Set the Android SDK path to `$ANDROID_HOME` (**Settings → Languages & Frameworks → Android SDK**).
4. Open the project root as a Gradle project and wait for the initial sync to complete.
5. Use the Gradle tool window or the terminal to run build and install tasks.

---

## Running on a device or emulator

### Android emulator (AVD)

```bash
# List available system images
sdkmanager --list | grep "system-images"

# Install a system image
sdkmanager "system-images;android-36;google_apis;x86_64"

# Create a virtual device
avdmanager create avd -n Pixel9 -k "system-images;android-36;google_apis;x86_64"

# Start the emulator
emulator -avd Pixel9

# Verify the device is detected
adb devices
```

### Physical device

1. Enable **Developer options** on the device (tap Build number 7 times).
2. Enable **USB debugging**.
3. Connect via USB and confirm the prompt on the device.
4. Verify with `adb devices`.

### Install the debug APK

```bash
# Unix / macOS
./gradlew app:installDebug

# Windows (PowerShell or cmd)
gradlew app:installDebug
```

> Make sure the backend is running and reachable from the device before launching the app. Use `10.0.2.2:8080` for the emulator or the host machine's LAN IP for a physical device.

### ADB — useful commands

```bash
# List connected devices
adb devices

# Install the APK manually (alternative to Gradle)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Stream all logcat output filtered to the app
adb logcat --pid=$(adb shell pidof -s com.edgareldy.tutorial)

# Stream logcat filtered by tag
adb logcat -s TutorialApp

# Clear the logcat buffer
adb logcat -c

# Save logcat to a file (useful for sharing crash reports)
adb logcat > logs.txt

# Restart adb server (fixes "device offline" or "unauthorized" errors)
adb kill-server && adb start-server
```

---

## Build and test

Tasks can be run in two ways:
- **IntelliJ / Android Studio** — open the **Run Anything** dialog (`Ctrl+Alt+R`), type `gradle <task>`
- **Terminal** — prefix with `./gradlew` on Unix/macOS or `gradlew` on Windows

The table below uses the IntelliJ format; the terminal equivalent is always `./gradlew <task>`.

### Build

| Task | Description |
|---|---|
| `gradle app:assembleDebug` | Compile the debug APK |
| `gradle app:assembleRelease` | Compile the release APK (requires signing config) |
| `gradle app:assembleDebugAndroidTest` | Compile the instrumented test APK |
| `gradle app:assembleDebugUnitTest` | Compile the debug unit test classes |
| `gradle app:assemble` | Compile all variants |
| `gradle app:build` | Compile + run all checks |
| `gradle app:clean` | Delete build outputs |

### Install / Uninstall

| Task | Description |
|---|---|
| `gradle app:installDebug` | Compile + install debug APK on connected device |
| `gradle app:uninstallDebug` | Uninstall the debug variant |
| `gradle app:uninstallAll` | Uninstall all variants |

### Verification

| Task | Description |
|---|---|
| `gradle app:testDebugUnitTest` | JVM unit tests — debug variant (no device required) |
| `gradle app:testReleaseUnitTest` | JVM unit tests — release variant |
| `gradle app:connectedDebugAndroidTest` | Instrumented tests — debug variant (device required) |
| `gradle app:lintDebug` | Lint — debug variant |
| `gradle app:lintRelease` | Lint — release variant |

### Analysis / Inspection

| Task | Description |
|---|---|
| `gradle app:analyzeDebugDependencies` | Analyze debug dependency tree |
| `gradle app:analyzeReleaseDependencies` | Analyze release dependency tree |
| `gradle app:analyzeDebugUnitTestDependencies` | Analyze debug unit test dependencies |
| `gradle app:androidDependencies` | Print Android-specific dependency tree |
| `gradle app:dependencies` | Print the full dependency tree |
| `gradle app:tasks` | List all available tasks for `:app` |

### Build variants

| Variant | Description |
|---|---|
| `debug` | Cleartext HTTP allowed, logging enabled, debuggable |
| `release` | HTTPS only, minified, signing config required |

---

## Branching strategy

| Branch | Scope |
|---|---|
| `master` | Stable and production-ready. No direct commits. |
| `develop` | Integration branch for all feature work. |
| `feature/core-architecture` | Project setup, Gradle Wrapper, Kotlin DSL, version catalog, OpenAPI client generation, Hilt, Retrofit/OkHttp, auth interceptor, secure storage, DataStore, Material 3 theme, Navigation Compose, shared composables. The app must build and show an empty scaffold before this branch closes. |
| `feature/auth` | Sign in, register, account activation (OTP), session restoration, token refresh, logout, 3-step mobile password reset, account profile display. |
| `feature/product-catalog` | Home screen, category listing, product listing with search and price filtering, product detail with image gallery and add-to-cart, bottom navigation shell (Home, Cart, Orders, Profile). |
| `feature/shopping-cart` | Cart read and mutations, server-calculated totals, checkout flow, order confirmation, duplicate-submit protection. |
| `feature/orders` | Order history and order detail. |

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) with a descriptive body explaining the **why**, not just the what.

```
feat(auth): implement sign-in flow

Connect the sign-in screen to the generated authentication API client.
Persist tokens using the Keystore-backed secure storage abstraction
and expose session state through the auth repository.
```

---

## feature/core-architecture

Technical foundation shared by every subsequent branch. The branch closes only when `./gradlew assembleDebug` succeeds and the app launches on an emulator showing an empty scaffold.

### Tasks

- [ ] `settings.gradle.kts`: project name, included modules
- [ ] Root `build.gradle.kts`: AGP, Kotlin, Hilt plugin versions
- [ ] `app/build.gradle.kts`: namespace, min/target SDK, Kotlin options, Hilt plugin, dependencies
- [ ] `gradle/libs.versions.toml`: all dependency versions pinned in the version catalog
- [ ] `gradle.properties`: `useAndroidX`, `enableJetifier`, heap settings
- [ ] Gradle Wrapper at a pinned version
- [ ] `TutorialApplication.kt` annotated `@HiltAndroidApp`
- [ ] `MainActivity.kt` with `setContent { }` Compose entry point
- [ ] Material 3 theme: color scheme, typography, `TutorialTheme` composable, light/dark support
- [ ] Shared composables: `LoadingIndicator`, `ErrorScreen`, `EmptyState`, `PrimaryButton`
- [ ] Navigation Compose: `NavHost`, sealed `Screen` route definitions, auth graph / main graph split
- [ ] `NetworkModule` (Hilt): `OkHttpClient`, `Retrofit`, `Kotlinx Serialization` converter
- [ ] `AuthInterceptor`: attaches `Authorization: Bearer <token>` header to every authenticated request
- [ ] `TokenAuthenticator`: handles 401, calls refresh endpoint, retries the original request; serializes concurrent refresh requests
- [ ] `SecureStorageModule` (Hilt): Android Keystore-backed encrypted credential store
- [ ] `DataStoreModule` (Hilt): DataStore instance for non-sensitive preferences
- [ ] `DispatchersModule` (Hilt): named `@IoDispatcher`, `@MainDispatcher`, `@DefaultDispatcher`
- [ ] `TutorialDatabase` (Room): database class, schema version 1
- [ ] `DatabaseModule` (Hilt): Room database instance and DAOs
- [ ] OpenAPI spec snapshot script: `curl http://localhost:8080/v3/api-docs -o openapi/openapi.json`
- [ ] OpenAPI Generator configuration (`openapi-generator-config.yaml`): Kotlin target, Retrofit2, Kotlinx Serialization
- [ ] Generated API client compiles without errors
- [ ] `debug` build type: cleartext HTTP allowed, `network_security_config.xml`
- [ ] `release` build type: minify enabled, ProGuard rules skeleton
- [ ] `./gradlew assembleDebug` passes

---

## feature/auth

All authentication screens, session management and the mobile OTP password reset flow.

### Screens

| Screen | Description |
|---|---|
| Sign in | Email + password, form validation, error display |
| Register | First name, last name, username, email, password + confirmation |
| Activate account | 6-digit OTP entry after registration |
| Forgot password | Email entry to request a reset OTP |
| Verify OTP | 6-digit OTP entry before showing the new-password screen |
| Reset password | New password + confirmation |
| Profile | Display of user information from `/auth/me`, sign-out button |

### API operations

| Operation | Endpoint |
|---|---|
| Sign in | `POST /api/v1/auth/login` |
| Register | `POST /api/v1/auth/register` |
| Activate account | `POST /api/v1/auth/activate-account` |
| Logout | `POST /api/v1/auth/logout` |
| Refresh token | `POST /api/v1/auth/refresh-token` |
| Get profile | `GET /api/v1/auth/me` |
| Request OTP | `POST /api/v1/password-reset/mobile` |
| Verify OTP | `POST /api/v1/password-reset/verify-otp` |
| Reset password | `POST /api/v1/password-reset/confirm` |

### Tasks

- [x] Domain: `User` model
- [x] Domain: `AuthRepository` interface (login, register, activate, logout, refresh, me)
- [x] Domain: `PasswordResetRepository` interface (requestOtp, verifyOtp, confirmReset)
- [x] Domain: `SignInUseCase`, `RegisterUseCase`, `ActivateAccountUseCase`, `LogoutUseCase`
- [x] Domain: `RequestPasswordResetOtpUseCase`, `VerifyPasswordResetOtpUseCase`, `ResetPasswordUseCase`
- [x] Domain: `GetProfileUseCase`
- [x] Data: `AuthRepositoryImpl` using the generated API client
- [x] Data: `PasswordResetRepositoryImpl`
- [x] Data: `SecureTokenStorage`: read/write/clear `access_token` and `refresh_token`
- [x] `SignInScreen` + `SignInViewModel` + `SignInUiState`
- [x] `RegisterScreen` + `RegisterViewModel` + `RegisterUiState`
- [x] `ActivateAccountScreen` + `ActivateAccountViewModel`
- [x] `ForgotPasswordScreen` + `ForgotPasswordViewModel`
- [x] `VerifyOtpScreen` + `VerifyOtpViewModel`
- [x] `ResetPasswordScreen` + `ResetPasswordViewModel`
- [x] `ProfileScreen` + `ProfileViewModel`
- [x] Session restoration on app launch: check stored token, navigate to main or auth graph
- [x] `TokenAuthenticator` wired to `AuthRepository.refresh()`; prevent retry loops on refresh failure
- [x] Clear all credentials on logout
- [x] Never log or persist OTP values
- [x] Never call web password reset endpoints (`/api/v1/auth/forgot-password`)
- [x] Tests: `SignInViewModelTest`, `AuthRepositoryImplTest`, `SignInUseCaseTest`
- [x] Tests: `VerifyOtpViewModelTest` (+ all other ViewModel tests migrated to Turbine)
- [ ] Tests: `PasswordResetRepositoryImplTest`

---

## feature/product-catalog

Browsing experience: home screen, category navigation, product listing with search and filtering, and product detail.

### Screens

| Screen | Description |
|---|---|
| Home | Featured products, category chips, entry point to the catalog |
| Category list | Paginated list of categories |
| Product list | Paginated grid, filterable by category, name, min/max price |
| Product detail | Images, description, price, quantity picker, add-to-cart button |

### API operations

| Operation | Endpoint |
|---|---|
| List categories | `GET /api/v1/categories` |
| Category detail | `GET /api/v1/categories/{id}` |
| List products | `GET /api/v1/products` |
| Product detail | `GET /api/v1/products/{id}` |

### Tasks

- [x] Domain: `Product`, `Category`, `ProductImage` models
- [x] Domain: `ProductRepository` interface, `CategoryRepository` interface
- [x] Domain: `GetProductsUseCase` (with filter params), `GetProductByIdUseCase`
- [x] Domain: `GetCategoriesUseCase`, `RefreshProductsUseCase`
- [x] Data: `ProductRepositoryImpl`, `CategoryRepositoryImpl`
- [x] Data: mappers from generated DTOs to domain models
- [ ] `HomeScreen` + `HomeViewModel` + `HomeUiState`
- [ ] `CategoryListScreen` + `CategoryListViewModel`
- [x] `ProductListScreen` + `ProductListViewModel` (search query, category filter, pagination state)
- [x] `ProductDetailScreen` + `ProductDetailViewModel`
- [x] Coil image loading with placeholder and error fallback
- [x] Pull-to-refresh on list screens
- [x] Empty state when no products match the active filters
- [x] Room entities: `ProductEntity`, `CategoryEntity`, `ProductImageEntity`
- [x] DAOs: `ProductDao` (upsert, query by id, query with filters), `CategoryDao`
- [x] Repository offline strategy: return cached data immediately, then refresh from network in the background; propagate network errors only when cache is empty
- [x] Cache invalidation on pull-to-refresh
- [x] Tests: `ProductRepositoryImplTest`, `GetProductsUseCaseTest`, `ProductListViewModelTest`, `ProductDetailViewModelTest`
- [ ] Bottom navigation shell (Home, Cart, Orders, Profile) wired in the main graph

---

## feature/shopping-cart

Cart management and checkout.

### Screens

| Screen | Description |
|---|---|
| Cart | Line items with quantities, subtotals, server-calculated total, checkout button |
| Order confirmation | Order number, status and summary after a successful checkout |

### API operations

| Operation | Endpoint |
|---|---|
| Get cart | `GET /api/v1/cart` |
| Add item | `POST /api/v1/cart/items` |
| Update quantity | `PATCH /api/v1/cart/items/{itemId}` |
| Remove item | `DELETE /api/v1/cart/items/{itemId}` |
| Clear cart | `DELETE /api/v1/cart/items` |
| Checkout | `POST /api/v1/orders` |

### Tasks

- [x] Domain: `Cart`, `CartItem` models
- [x] Domain: `CartRepository` interface
- [x] Domain: `GetCartUseCase`, `AddCartItemUseCase`, `UpdateCartItemUseCase`, `RemoveCartItemUseCase`, `ClearCartUseCase`, `GetCartItemCountUseCase`
- [x] Domain: `CreateOrderUseCase` (checkout from cart, then clears cart via `ClearCartUseCase`)
- [x] Data: `CartRepositoryImpl`
- [x] `CartScreen` + `CartViewModel` + `CartUiState`
- [x] `CheckoutConfirmationScreen` + `CheckoutConfirmationViewModel` (order summary, confirm button, duplicate-submit protection)
- [x] Add-to-cart action wired from `ProductDetailScreen`
- [x] Cart item count badge on the navigation bar
- [x] Checkout button disabled while a request is in flight (duplicate-submit protection)
- [x] Display server-calculated total; never compute totals on the client
- [x] Tests: `CartViewModelTest`, `SharedCartViewModelTest`, `CreateOrderUseCaseTest`

---

## feature/orders

Order history and order detail.

### Screens

| Screen | Description |
|---|---|
| Order list | Paginated history with order number, date, total and status badge |
| Order detail | Full item breakdown, quantities, unit prices and order total |

### API operations

| Operation | Endpoint |
|---|---|
| List orders | `GET /api/v1/orders` |
| Order detail | `GET /api/v1/orders/{id}` |

### Tasks

- [ ] Domain: `Order`, `OrderItem` models, `OrderStatus` enum (`PENDING`, `CONFIRMED`, `CANCELLED`)
- [ ] Domain: `OrderRepository` interface
- [ ] Domain: `GetOrdersUseCase`, `GetOrderByIdUseCase`
- [ ] Data: `OrderRepositoryImpl`
- [ ] `OrderListScreen` + `OrderListViewModel` + `OrderListUiState`
- [ ] `OrderDetailScreen` + `OrderDetailViewModel`
- [ ] `OrderStatusBadge` composable: color-coded chip per status
- [ ] Pull-to-refresh on the order list
- [ ] Room entities: `OrderEntity`, `OrderItemEntity`
- [ ] DAOs: `OrderDao` (upsert, query all, query by id)
- [ ] Repository offline strategy: serve cached orders immediately, refresh on pull-to-refresh
- [ ] Tests: `OrderRepositoryImplTest`, `GetOrdersUseCaseTest`, `OrderListViewModelTest`

---

## Implementation status

| Phase | Branch | Status |
|---|---|---|
| 0 | Backend reconnaissance, OpenAPI spec snapshot | ✅ Complete |
| 1 | `feature/core-architecture` | ✅ Complete |
| 2 | `feature/auth` | ✅ Complete |
| 3 | `feature/product-catalog` | ⚠️ In progress (bottom nav missing) |
| 4 | `feature/shopping-cart` | ✅ Complete |
| 5 | `feature/orders` | 🔲 Pending |

### Phase 0 findings

The backend is running and the OpenAPI spec has been downloaded to `openapi/openapi.json`.

Key discoveries that affect the implementation:

| Finding | Impact |
|---|---|
| Account activation is required after registration | Activation screen (6-digit OTP) must be part of the sign-up flow |
| Token refresh is supported | Implement proactive refresh using `expires_in`; serialize concurrent requests |
| Mobile OTP reset has 3 steps, not 2 | An intermediate verify-OTP screen is needed before the new-password screen |
| OTP is passed as `?token=` query param on the confirm step | Generated client configuration must handle this correctly |
| `POST /api/v1/orders` takes no request body | The checkout button calls the endpoint directly; the backend reads the cart |
| `/api/v1/customers` is admin-only CRUD | User profile comes from `GET /api/v1/auth/me`, not from `/customers` |
| Mobile password reset path is `/api/v1/password-reset/`, not `/api/v1/auth/mobile/` | Earlier documentation drafts were inaccurate; the OpenAPI spec is the source of truth |

---

## Concepts covered

| Concept | Context |
|---|---|
| Kotlin idioms | Data classes, sealed classes, extension functions, coroutine scopes |
| Jetpack Compose | Stateless composables, state hoisting, side effects, lazy lists, theming |
| Material 3 | Color schemes, typography scale, component library, light/dark mode |
| Navigation Compose | Nested graphs, type-safe routes, back stack management |
| MVVM | ViewModel lifecycle, `StateFlow`, one-off event channels |
| Clean Architecture | Layered separation, dependency inversion, use cases |
| Hilt | Module setup, component scopes, injection in ViewModels and Compose |
| Retrofit + OkHttp | Service interfaces, interceptors, authenticator for token refresh |
| Kotlin Serialization | JSON deserialization, snake_case mapping, custom serializers |
| Coroutines and Flow | `viewModelScope`, `collectAsStateWithLifecycle`, error handling |
| Android Keystore | Symmetric encryption backed by hardware-secured key storage |
| DataStore | Proto DataStore for typed preferences |
| Coil | Async image loading, placeholder, error handling in Compose |
| OpenAPI Generator | Generating a typed Kotlin client from an OpenAPI spec |
| DTO-to-domain mapping | Isolating generated types from application models |
| Room | Local SQLite cache with DAOs, entities and schema migrations |
| Offline strategy | Cache-first with background refresh; network errors surfaced only on empty cache |
| Pagination | Server-side pagination with custom `PageResponse<T>` |
| Unit testing | Use cases, mappers, ViewModels with MockK and Turbine |
| API testing | Serialization and header tests with MockWebServer |
| UI testing | Navigation and form tests with Compose UI Test |
| Gradle Kotlin DSL | Build scripts, version catalog, build variants |
| GitFlow | Feature branches, Conventional Commits, merge discipline |
