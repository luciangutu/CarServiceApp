# CarServiceApp

CarServiceApp is a native Android app for tracking vehicle maintenance. It keeps a local record of cars, service history, maintenance intervals, and reusable service operation suggestions.

## Features

- Add and manage cars with make, model, year, license plate, VIN, and notes.
- Record service entries with date, kilometers, service name, operations, and cost.
- Define maintenance intervals by component, kilometers, and years.
- See upcoming or overdue maintenance from the dashboard and car detail screens.
- Back up and restore the local database through Android file sharing.

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- Room database
- Hilt dependency injection
- Navigation Compose
- Gradle Android Plugin

## Getting Started

Open the project in Android Studio, let Gradle sync, then run the `app` configuration on an emulator or Android device.

From the command line:

```bash
./gradlew assembleDebug
```

Run tests with:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Backup and Restore

Import and export are available from the app's Settings screen.

Export uses the `Backup to Google Drive / Share` button. The app locates the local Room database file named `car_service_database`, exposes it through the app `FileProvider`, and opens Android's share sheet so the file can be saved to Google Drive, sent to another app, or stored elsewhere.

Import uses the `Restore from File` button. The app opens Android's document picker, reads the selected file, closes the Room database, and overwrites the local `car_service_database` file with the selected backup. After a successful restore, restart the app so Room reopens the replaced database cleanly.

Backups are raw Room database files. Restore should use a backup created by this app and matching the current database schema version.

## Project Structure

- `app/src/main/java/com/lucian/carserviceapp/data`: Room entities, DAOs, database, and repository.
- `app/src/main/java/com/lucian/carserviceapp/ui`: Compose screens, navigation, theme, components, and view models.
- `app/src/main/java/com/lucian/carserviceapp/di`: Hilt modules.
