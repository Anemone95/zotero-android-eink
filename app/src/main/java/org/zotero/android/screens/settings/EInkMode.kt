package org.zotero.android.screens.settings

import androidx.annotation.StringRes
import org.zotero.android.uicomponents.Strings

enum class EInkMode(
    @StringRes val titleResId: Int,
) {
    Colorful(Strings.settings_e_ink_mode_colorful),
    Grayscale(Strings.settings_e_ink_mode_grayscale),
    Off(Strings.settings_e_ink_mode_off);
}
