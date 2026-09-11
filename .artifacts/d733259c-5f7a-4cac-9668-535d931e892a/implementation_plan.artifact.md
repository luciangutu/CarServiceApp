# Fix KSP Class Loading Error in AGP 9.4.0

The project is encountering `Unable to load class 'com.google.devtools.ksp.gradle.KspTaskJvm'`. This is due to a version mismatch between the Android Gradle Plugin (AGP) 9.4.0, Kotlin 2.2.10, and older versions of Hilt and Room that depend on internal KSP classes that were removed in KSP 2.x.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts (root)](file:///home/lucian/Dropbox/Other/CarServiceApp/build.gradle.kts)
- Update Dagger Hilt to `2.60.1`.
- Add `buildscript` block with KSP classpath to ensure correct class loading as recommended for AGP 9.0+.

#### [MODIFY] [app/build.gradle.kts](file:///home/lucian/Dropbox/Other/CarServiceApp/app/build.gradle.kts)
- Update Hilt to `2.60.1`.
- Update Room to `2.8.5`.
- Migrate `kotlinOptions` to `compilerOptions` as required for Kotlin 2.x and AGP 9.x.

#### [MODIFY] [gradle.properties](file:///home/lucian/Dropbox/Other/CarServiceApp/gradle.properties)
- Enable `android.builtInKotlin=true` and `android.newDsl=true` to properly support AGP 9.4.0 features (optional but recommended, I will keep them for now but migrate the code to be compatible). Actually, search results suggest that if these are `false`, `kotlin-android` plugin might fail with other errors. I'll enable them to move to the supported path.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify the build completes without class loading errors.
