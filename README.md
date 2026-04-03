# Zotero Android for E-Ink Devices

This repository is a custom build of Zotero Android made for e-ink devices.


## What was added

- A rotate button in the first row of PDF reader actions.
- A crop feature for the PDF reader that removes left and right page margins for a more full-width reading view.
- Disallow horizontal page movement using single finger.
- Separate motion between finger and pen.
  - Finger can move and zoom the PDF, Pen and eraser do not trigger these.
  - Pen long-press activates text selection word by word.
- Default PDF scroll direction is set to `Vertical`.
- Default PDF page mode is set to `Single`.

## TODO
[] translate?
[] different color in non-color monitor

## Build

To build the debug APK:

```bash
./gradlew :app:assembleDevDebug
```

The generated APK will be located at:

```text
app/build/outputs/apk/dev/debug/app-dev-debug.apk
```
