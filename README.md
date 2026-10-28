# Mynesweeper

The original Java/libGDX Android Minesweeper game, restored with its gray board,
yellow controls, digital counters, and 8-by-11 layout with 20 mines.

## Play

- Tap **REVEAL** / **FLAG** to choose a mode, then tap a covered tile.
- A first reveal is always safe. Empty cells open their connected safe area.
- Clear every safe cell to win; flags are optional.
- Tap **NEW** to restart. Holding the mode button also starts a new game.

## Desktop development

Use JDK 17 or newer (the Gradle wrapper also supports JDK 25).
An Android SDK is not required for desktop development.

```powershell
.\gradlew.bat :desktop:run
.\gradlew.bat :core:test :desktop:installDist
```

The runnable distribution is under `desktop/build/install/desktop/`.
On Windows, run its `bin/desktop.bat` launcher. Assets are bundled.

For a graphics integration test on a machine with a display/OpenGL support:

```powershell
.\gradlew.bat :desktop:smokeTest
```

This checks first-tap safety, mode switching, repeated restart, and input rebinding,
and writes `.validation/game.png` from the actual renderer.

## Android

Install Android SDK Platform 36 and Build Tools 36.0.0. Set `sdk.dir` in a local
`local.properties` file, or set `ANDROID_HOME`. Enable the Android module with
`-Pandroid` (also set this Gradle project property when importing Android Studio).

```powershell
.\gradlew.bat -Pandroid :android:assembleDebug
```

The test APK is `android/build/outputs/apk/debug/android-debug.apk`.
It supports Android 7.1+ and is signed with a development key, not a release key.
The restored application ID is `com.tjstudios.mynesweeper`.

Release signing, store submission, and verification on a physical Android device
are separate from the desktop checks. There is no iOS launcher in this repository.

## Restoration

- Completed bounded, iterative zero-cell expansion, preserving flags.
- Added first-reveal safety and victory by clearing safe cells.
- Fixed restart input, reset win/loss state, stopped the timer after completion,
  and kept the end state rendering so restart remains usable.
- Fixed tile sizing and viewport projection, added an explicit New Game control,
  and removed repeated texture/font leaks.
- Updated to libGDX 1.14.2, LWJGL3, Gradle 9.1, and Android Gradle Plugin 9.0.1.
- Added gameplay regression tests and a graphics integration test.
