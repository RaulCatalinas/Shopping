# Contributing to Shopping

Thanks for considering contributing! This is a small, indie-maintained project, so please keep pull
requests focused and reasonably small — easier to review, easier to merge.

## Getting started

1. Fork the repo and clone your fork.
2. Open the project in Android Studio and let Gradle sync.
3. Create a `local.properties` file in the project root (it is git-ignored):
   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_PUBLISHABLE_KEY=your-publishable-key
   GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
   ```
4. Set up your own Supabase project for local development — the tables, policies and functions are
   documented in `/docs` (or ask in an issue if you get stuck). You will also need your own Google
   Cloud OAuth clients (a web client for Supabase and an Android client with your debug SHA-1).
5. Run the app on a device or emulator with Android 12 (API 31) or newer.

Never commit `local.properties`, keystores, or any secret.

## Code style

- Follow the [official Kotlin code style](https://kotlinlang.org/docs/coding-conventions.html). Use
  **Code → Reformat Code** and **Optimize Imports** in Android Studio before opening a PR.
- Run `./gradlew lintDebug` and fix any new warnings you introduce.
- No wildcard imports: import each class explicitly.
- Group code by feature, not only by layer (for example `backend/auth`, `screens/settings`). Only
  code used by several features belongs in `shared/`.
- Reusable UI lives in `shared/components`.
- Architecture: MVVM with Hilt. ViewModels expose state with `StateFlow`; screens read it with
  `collectAsStateWithLifecycle`.
- Rethrow `CancellationException` in `suspend` functions instead of swallowing it in a generic
  `catch`.
- Do not hardcode UI text: add it to `res/values/strings.xml` and its Spanish translation to
  `res/values-es/strings.xml` in the same PR. Keep the tone simple and friendly, since the app is
  aimed at non-technical users.

## Commit messages

This repo follows [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(auth): add sign in with Google
fix(profile): reopen confirm dialog after dismissing it
refactor(settings): store app language in the system instead of DataStore
docs: update README setup instructions
```

## Pull requests

1. Branch off `main`: `feature/short-description` or `fix/short-description`.
2. Keep PRs scoped to one change — split unrelated changes into separate PRs.
3. Make sure the app builds and `./gradlew lintDebug` and `./gradlew testDebugUnitTest` pass.
4. If you change the database (tables, policies or functions), include the SQL in the PR.
5. Describe **what** changed and **why** in the PR description; link any related issue.
6. Be patient — this is maintained in spare time, reviews may take a few days.

## Reporting bugs / suggesting features

Open an issue with:

- What you expected to happen vs. what actually happened
- Steps to reproduce (for bugs)
- Device, Android version and app version (for bugs)
- The relevant Logcat output, if the app crashed

## License

By contributing, you agree that your contributions will be licensed under the
project's [GPLv3 license](./LICENSE).
