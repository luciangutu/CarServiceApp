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

Export uses the `Backup to Google Drive / Share` button. The app generates a structured JSON file containing all vehicle data, service logs, maintenance intervals, and operation suggestions. This file is shared through the Android share sheet for saving to cloud storage or other apps.

Import uses the `Restore from File` button. The app opens a document picker to select a previously exported JSON backup. It then clears the current local database and performs a safe, sequential import of all entities, maintaining database integrity and relationships.

Backups are stored in a portable JSON format, making them resilient to future database schema updates. A backup includes:
- Cars (including current odometer readings)
- Service History Entries
- Maintenance Intervals
- Operation Suggestions

## Project Structure

- `app/src/main/java/com/github/luciangutu/carserviceapp/data`: Room entities, DAOs, database, and repository.
- `app/src/main/java/com/github/luciangutu/carserviceapp/ui`: Compose screens, navigation, theme, components, and view models.
- `app/src/main/java/com/github/luciangutu/carserviceapp/di`: Hilt modules.
