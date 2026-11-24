# Branch: feature/auth

This branch implements the complete authentication layer: sign-in, registration with email verification, session restoration, transparent token refresh and a 3-step mobile OTP password reset flow.

---

## What you will learn

- How to implement the full authentication lifecycle on Android (login, logout, session restore)
- How to persist tokens securely and restore a session on cold start
- How to wire a `TokenAuthenticator` to handle token expiry without any screen-level code
- How to build a multi-step form flow in Compose with shared state between screens
- How the mobile OTP password reset differs from the web flow, and why the endpoints are different
- How to test a ViewModel that emits a sequence of states using Turbine

---

## What it delivers

### Session restoration on app launch

When the app starts, `AppNavigation` reads the stored `access_token` from `SecureTokenStorage`. If a token exists, the user is sent directly to the main graph. If not, they see the sign-in screen. This check happens before the first frame is drawn using `SplashScreen` or a simple conditional in `NavHost`.

No token validity check is made at this point — the `TokenAuthenticator` handles expiry lazily when the first authenticated request returns a 401.

### Sign-in and registration

`SignInViewModel` and `RegisterViewModel` follow the same pattern:
1. The user fills the form.
2. The ViewModel validates local rules (non-empty fields, password length) before making any network call.
3. On submit, the ViewModel calls the use case, sets `Loading` state (disabling the submit button), then sets `Success` or `Error`.

The `Error` state carries a human-readable message extracted from the API's `ProblemDetail` response body. After the error is shown, `errorShown()` resets the state to `Idle` so the user can try again without stale error state blocking the form.

### Account activation (OTP)

After registration, the backend sends a 6-digit code to the user's email. The `ActivateAccountScreen` presents an OTP field and submits to `POST /api/v1/auth/activate-account`. On success, navigation moves to sign-in.

The activation code is held only in transient form state. It is never persisted or logged.

### Mobile password reset (3 steps)

The web password reset endpoints (`/api/v1/auth/forgot-password`) are designed for the Angular frontend and require a callback URL. The mobile flow uses a separate set of endpoints under `/api/v1/password-reset/`:

| Step | Endpoint | Purpose |
|---|---|---|
| 1 | `POST /mobile` | User enters their email; backend sends a 6-digit OTP |
| 2 | `POST /verify-otp` | User enters the OTP; backend validates without consuming it |
| 3 | `POST /confirm?token={otp}` | User enters new password; backend consumes the OTP and resets |

Step 2 exists so the user can be shown the new-password screen with confidence that their OTP is valid, before they invest time typing a new password. The OTP is passed as a query parameter at step 3, not in the request body.

The OTP value travels as form state through the navigation stack. It is never stored to disk.

### Token refresh wiring

`TokenAuthenticator` is wired to the `AuthRepository.refresh()` use case. When a 401 is received:

1. The authenticator calls `refresh()` using the `@AuthOnly` secondary `OkHttpClient`.
2. If refresh succeeds, new tokens are saved and the original request is retried with the updated header.
3. If refresh fails (the refresh token is also expired), all credentials are cleared and the user is sent back to sign-in.

The redirect to sign-in from inside `TokenAuthenticator` requires a `SharedFlow` or similar mechanism because `TokenAuthenticator` runs on the OkHttp thread, not on the main thread.

### Profile screen

`ProfileScreen` calls `GET /api/v1/auth/me` to display the user's profile information. This endpoint is correct for the authenticated user's own data. The `/api/v1/customers` endpoints are admin-only and are never called from the mobile client.

The sign-out button calls `LogoutUseCase`, which posts to `POST /api/v1/auth/logout` and then clears all stored tokens regardless of the response status.

---

## Key files

| File | Layer | Role |
|---|---|---|
| `AuthRepositoryImpl.kt` | data | Login, register, activate, logout, refresh, me |
| `PasswordResetRepositoryImpl.kt` | data | 3-step OTP reset |
| `AuthApiService.kt` | data | Retrofit interface for `/api/v1/auth/` |
| `PasswordResetApiService.kt` | data | Retrofit interface for `/api/v1/password-reset/` |
| `SignInUseCase.kt` | domain | Validates credentials and calls the repository |
| `RegisterUseCase.kt` | domain | Submits registration, returns activation required signal |
| `LogoutUseCase.kt` | domain | Calls logout endpoint and clears local tokens |
| `ResetPasswordUseCase.kt` | domain | Step 3 of the OTP reset flow |
| `SignInViewModel.kt` | presentation | Form state, loading, error handling |
| `VerifyOtpViewModel.kt` | presentation | OTP validation at step 2 |
| `ResetPasswordViewModel.kt` | presentation | New password form at step 3 |
| `ProfileViewModel.kt` | presentation | Calls `GetProfileUseCase` on launch |
| `AuthModule.kt` | DI | Provides `AuthRepository`, `PasswordResetRepository` |
