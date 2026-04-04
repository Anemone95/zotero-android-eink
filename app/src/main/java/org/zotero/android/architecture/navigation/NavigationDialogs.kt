package org.zotero.android.architecture.navigation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import org.zotero.android.ZoteroApplication
import org.zotero.android.screens.settings.EInkMode
import org.zotero.android.uicomponents.theme.CustomTheme

fun NavGraphBuilder.dialogFixedDimens(
    modifier:Modifier,
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable () -> Unit,
) {
    customDialog(
        route = route,
        dialogModifier = modifier,
        arguments = arguments,
        content = content
    )
}

fun NavGraphBuilder.dialogFixedMaxHeight(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable () -> Unit,
) {
    customDialog(
        route = route,
        arguments = arguments,
        dialogModifier = Modifier.requiredHeightIn(max = 400.dp),
        content = content
    )
}

fun NavGraphBuilder.dialogDynamicHeight(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable () -> Unit,
) {
    customDialog(
        route = route,
        arguments = arguments,
        dialogModifier = Modifier.fillMaxHeight(0.8f),
        content = content
    )
}

private fun NavGraphBuilder.customDialog(
    route: String,
    dialogModifier: Modifier,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable () -> Unit,
) {
    dialog(
        route = route,
        arguments = arguments,
        dialogProperties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        ApplyDialogWindowSettings()
        val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        dispatcher?.onBackPressed()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = dialogModifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Prevent taps inside the dialog from dismissing it.
                        }
                    }
                    .clip(shape = RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = CustomTheme.colors.dialogBorderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ApplyDialogWindowSettings() {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window ?: return
    val isEInkModeEnabled = ZoteroApplication.instance.defaults.getEInkMode() != EInkMode.Off

    SideEffect {
        if (isEInkModeEnabled) {
            dialogWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogWindow.setDimAmount(0f)
            dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialogWindow.setWindowAnimations(0)
            dialogWindow.decorView.elevation = 0f
        }
    }
}
