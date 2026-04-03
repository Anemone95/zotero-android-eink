package org.zotero.android.pdf.data


enum class PageScrollMode {
 JUMP, CONTINUOUS
}

enum class PageLayoutMode {
    SINGLE,
    DOUBLE,
    AUTOMATIC,
}

enum class PageScrollDirection {
    HORIZONTAL, VERTICAL
}

enum class PageFitting {
    FIT, FILL, CROP
}

enum class PageAppearanceMode {
    LIGHT, DARK, AUTOMATIC
}

enum class LandscapeOrientation {
    NORMAL, REVERSE
}

data class PDFSettings(
    var transition: PageScrollMode,
    var pageMode: PageLayoutMode,
    var direction: PageScrollDirection,
    var pageFitting: PageFitting,
    var appearanceMode: PageAppearanceMode,
    var landscapeOrientation: LandscapeOrientation?,
    var allowsSingleFingerHorizontalPageMovement: Boolean,
    var idleTimerDisabled: Boolean,
) {
    companion object {
        fun default(): PDFSettings {
            return PDFSettings(
                transition = PageScrollMode.CONTINUOUS,
                pageMode = PageLayoutMode.SINGLE,
                direction = PageScrollDirection.VERTICAL,
                pageFitting = PageFitting.FIT,
                appearanceMode = PageAppearanceMode.AUTOMATIC,
                landscapeOrientation = LandscapeOrientation.REVERSE,
                allowsSingleFingerHorizontalPageMovement = false,
                idleTimerDisabled = false
            )
        }
    }
}
