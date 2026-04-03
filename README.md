# Zotero Android for E-Ink Devices

This repository is a custom build of Zotero Android made for e-ink devices.


## What was added

- A rotate button in the first row of PDF reader actions.
- Default PDF scroll direction is set to `Vertical`.
- Default PDF page mode is set to `Single`.


## Build

To build the debug APK:

```bash
./gradlew :app:assembleDevDebug
```

The generated APK will be located at:

```text
app/build/outputs/apk/dev/debug/app-dev-debug.apk
```
