# Zotero Android for E-Ink Devices

This repository is a custom build of Zotero Android made for e-ink devices.


## What was added

- A crop feature for the PDF reader that removes left and right page margins for a more full-width reading view.
- Disallow horizontal page movement using single finger.
- mark annotation which has notes
- Default PDF scroll direction is set to `Vertical`.
- Default PDF page mode is set to `Single`.
- Translation
  - Gemini
  - DeepL free
  - Viwoods AI
  - Offline (Bergamot) — fully offline en→zh via the [`net.wenyuanxu.translate`](https://github.com/Anemone95/offline-translate) AIDL service. Install that app first; no API key needed.
- E-Ink Mode in settings.
  - Grayscale e-ink has a rotate button in the first row of PDF reader actions for rotate the screen.
  - pop-up windows do not have shadows or background dimming.
  - E-ink-specific word-based text selection in the PDF reader: long-press selects the word under the touch point, dragging expands the text selection word by word.
  - Grayscale e-ink different highlight visualization

## TODO
* Pen writing
 
## Build

To build the debug APK:

```bash
./gradlew :app:assembleDevDebug
```

The generated APK will be located at:

```text
app/build/outputs/apk/dev/debug/app-dev-debug.apk
```
