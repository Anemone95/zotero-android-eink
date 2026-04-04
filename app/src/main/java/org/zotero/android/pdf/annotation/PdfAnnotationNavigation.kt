package org.zotero.android.pdf.annotation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.zotero.android.architecture.navigation.ZoteroNavigation
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.pdf.annotation.data.PdfAnnotationArgs
import org.zotero.android.screens.tagpicker.TagPickerScreen
import org.zotero.android.uicomponents.navigation.ZoteroNavHost

@Composable
internal fun PdfAnnotationNavigation(
    args: PdfAnnotationArgs,
    onBack: () -> Unit,
    disableAnimations: Boolean = false,
) {
    val navController = rememberNavController()
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navigation = remember(navController) {
        ZoteroNavigation(navController, dispatcher)
    }

    val layoutType = CustomLayoutSize.calculateLayoutType()
    val isTablet = layoutType.isTablet()
    if (!isTablet) {
        BackHandler(onBack = {
            onBack()
        })
    }
    ZoteroNavHost(
        navController = navController,
        startDestination = PdfAnnotationDestinatiosn.PDF_ANNOTATION_SCREEN,
        enterTransition = { if (disableAnimations) EnterTransition.None else slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { if (disableAnimations) ExitTransition.None else slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { if (disableAnimations) EnterTransition.None else slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { if (disableAnimations) ExitTransition.None else slideOutHorizontally(targetOffsetX = { it }) },
    ) {
        pdfAnnotationNavScreens(args = args, navigation = navigation, onClose = onBack)
    }
}

internal fun NavGraphBuilder.pdfAnnotationNavScreens(
    args: PdfAnnotationArgs,
    navigation: ZoteroNavigation,
    onClose: () -> Unit,
) {

    pdfAnnotationScreen(
        args = args,
        onBack = onClose,
        navigateToTagPicker = navigation::toTagPicker
    )
    tagPickerScreen(onBack = navigation::onBack)
}

private fun NavGraphBuilder.pdfAnnotationScreen(
    args: PdfAnnotationArgs,
    onBack: () -> Unit,
    navigateToTagPicker: () -> Unit,
) {
    composable(
        route = PdfAnnotationDestinatiosn.PDF_ANNOTATION_SCREEN,
        arguments = listOf(),
    ) {
        PdfAnnotationScreen(
            args = args,
            onBack = onBack,
            navigateToTagPicker = navigateToTagPicker
        )
    }
}

private fun NavGraphBuilder.tagPickerScreen(
    onBack: () -> Unit,
) {
    composable(
        route = PdfAnnotationDestinatiosn.TAG_PICKER_SCREEN,
    ) {
        TagPickerScreen(onBack = onBack)
    }
}

private object PdfAnnotationDestinatiosn {
    const val PDF_ANNOTATION_SCREEN = "pdfAnnotationScreen"
    const val TAG_PICKER_SCREEN = "tagPickerScreen"
}

fun ZoteroNavigation.toPdfAnnotationScreen() {
    navController.navigate(PdfAnnotationDestinatiosn.PDF_ANNOTATION_SCREEN)
}

private fun ZoteroNavigation.toTagPicker() {
    navController.navigate(PdfAnnotationDestinatiosn.TAG_PICKER_SCREEN)
}
