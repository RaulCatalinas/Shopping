# Shopping

A collaborative shopping list app for Android, built to be shared and edited in real time between
multiple users and devices.

## What is this?

Shopping is a shopping list app built to be shared with zero friction: add, remove, or check off an
item and it shows up for everyone on the list. It is designed for non-technical users, so the
interface stays simple and every text is available in English and Spanish.

## Features

- ✅ Sign up and log in with email and password
- ✅ Sign in with Google (Credential Manager)
- ✅ Password recovery through an email link that opens the app
- ✅ Profile screen with live username availability check
- ✅ Delete your account and data from the app
- 🎨 Light, dark and system themes
- 🌍 English and Spanish, switchable inside the app without restarting

## Roadmap

- [ ] Shopping lists data model
- [ ] Share a list with other users
- [ ] Real-time sync between devices (Supabase Realtime)
- [ ] Offline support
- [ ] Google Play release

## Tech stack

| Layer                | Technology                                              |
|----------------------|---------------------------------------------------------|
| Language             | Kotlin                                                  |
| UI                   | Jetpack Compose + Material 3                            |
| Architecture         | MVVM (ViewModel + StateFlow), repositories              |
| Dependency injection | Hilt                                                    |
| Backend              | Supabase (Auth + Postgres through PostgREST)            |
| Authentication       | Email/password (PKCE) and Google via Credential Manager |
| Local preferences    | Jetpack DataStore                                       |
| Networking           | Ktor (Android engine)                                   |
| Serialization        | kotlinx.serialization                                   |
| Localization         | Android string resources + AppCompat per-app locales    |

## Architecture

- **Screens** (`screens/`): Compose UI and the ViewModels that expose its state.
- **Backend** (`backend/`): repositories and providers that talk to Supabase and Google, grouped by
  feature (`auth`, `db`).
- **Shared** (`shared/`): reusable components, extensions and types used by several features.
- **Dependency injection** (`di/`): Hilt modules, including the Supabase client.
- **Server-side logic**: operations that must be atomic or secure (such as creating a profile or
  checking a username) run as Postgres functions called through RPC.

## Requirements

- Android 12 (API 31) or newer
- Android Studio with a recent stable Kotlin and Android Gradle Plugin
- A Supabase project
- A Google Cloud project with an OAuth **web** client (for Google sign-in)

## Getting started

1. Clone the repository.
2. Create a `local.properties` file in the project root (it is git-ignored) with:
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_PUBLISHABLE_KEY=your-publishable-key
   GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
   ```
3. Set up your own Supabase project: tables, policies and functions are documented in `/docs`.
4. In Supabase, enable the Google provider with your web client ID and secret, and add
   `shopping://reset-password` to the allowed redirect URLs.
5. In Google Cloud, create an Android OAuth client with the app package name and your debug SHA-1
   (`./gradlew signingReport`).
6. Open the project in Android Studio and run the `app` configuration.

Never commit `local.properties`, keystores or any client secret.

## Platforms

Android only. iOS is not planned for now.

## License

Private — no license defined yet.
