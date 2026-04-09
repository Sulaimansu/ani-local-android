# AniLocal Android

**Repo Setup Instructions:**

1.  **Create Repository:**
    ```bash
    mkdir ani-local-android
    cd ani-local-android
    git init
    ```
2.  **Remote Connection:**
    ```bash
    git remote add origin https://github.com/<your-username>/ani-local-android.git
    ```
3.  **Initial Commit:**
    ```bash
    git add .
    git commit -m "chore: initial scaffold with aniList integration and local db"
    git branch -M main
    git push -u origin main
    ```

**Project Overview:**
-   **Architecture:** MVVM + Clean Architecture.
-   **UI:** Jetpack Compose (Modern, Rich Styling, AniList-inspired).
-   **Local DB:** Room Database (Offline-first for saved anime).
-   **Network:** Apollo GraphQL Client (AniList API).
-   **Background Sync:** WorkManager (Daily airing schedule updates).
-   **DI:** Hilt.
-   **Async:** Kotlin Coroutines & Flow.

**Key Features:**
-   Search AniList via GraphQL.
-   Save anime locally with full metadata (description, tags, relations, status).
-   Classify anime (Watching, Completed, Planning, etc.).
-   Airing countdowns with daily auto-sync.
-   Offline access to all saved data.
-   Rich UI: Gradients, detailed cards, smooth transitions, no minimalism.

**Prerequisites:**
-   Android Studio Koala or newer.
-   JDK 17.
-   No API Key required for AniList public queries.

**Build:**
```bash
./gradlew assembleDebug
```

**Notes for Low-End Hardware:**
-   Enable Gradle configuration cache in `gradle.properties`.
-   Use `./gradlew` commands over IDE buttons to save RAM.
-   Disable unused modules in `settings.gradle.kts` if needed.