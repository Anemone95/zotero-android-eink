# Zotero Android for E-Ink Devices

This repository is a custom build of Zotero Android made for e-ink devices.


## What was added

- A crop feature for the PDF reader that removes left and right page margins for a more full-width reading view.
- Disallow horizontal page movement using single finger.
- translate
  - Gemini support
  - DeepL free support
- E-Ink Mode in settings.
- Grayscale e-ink has a rotate button in the first row of PDF reader actions for rotate the screen.
- E-Ink Mode pop-up windows do not have shadows or background dimming.
- E-ink-specific word-based text selection in the PDF reader: long-press selects the word under the touch point, dragging expands the text selection word by word.
- E-ink different color in non-color monitor
- Default PDF scroll direction is set to `Vertical`.
- Default PDF page mode is set to `Single`.

## TODO

## Build

To build the debug APK:

```bash
./gradlew :app:assembleDevDebug
```

The generated APK will be located at:

```text
app/build/outputs/apk/dev/debug/app-dev-debug.apk
```
