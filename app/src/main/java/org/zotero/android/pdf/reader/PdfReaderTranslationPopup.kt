package org.zotero.android.pdf.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.zotero.android.uicomponents.Strings
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun PdfReaderTranslationPopup(
    viewState: PdfReaderViewState,
    viewModel: PdfReaderViewModel,
) {
    val popup = viewState.translationPopup ?: return
    var popupSize by remember(popup.anchorX, popup.anchorY) { mutableStateOf(IntSize.Zero) }
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.dismissTranslationPopup() }
                )
            }
    ) {
        val containerWidth = constraints.maxWidth
        val popupMaxHeight = maxHeight * 0.6f
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = popupMaxHeight)
                .onSizeChanged { popupSize = it }
                .offsetForAnchor(
                    anchorX = popup.anchorX,
                    anchorY = popup.anchorY,
                    containerWidth = containerWidth,
                    containerHeight = constraints.maxHeight,
                    popupSize = popupSize,
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {}
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    popup.isLoading -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = stringResource(Strings.pdf_translate_loading),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    popup.errorMessage != null -> {
                        Text(
                            text = popup.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    else -> {
                        Text(
                            text = popup.translation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Clip,
                        )
                    }
                }

                if (!popup.isLoading && popup.errorMessage == null) {
                    Button(
                        onClick = viewModel::addTranslatedSelectionAsAnnotation,
                    ) {
                        Text(text = stringResource(Strings.add))
                    }
                }
            }
        }
    }
}

private fun Modifier.offsetForAnchor(
    anchorX: Int,
    anchorY: Int,
    containerWidth: Int,
    containerHeight: Int,
    popupSize: IntSize,
): Modifier {
    return this.offset {
        val verticalMargin = 16.dp.roundToPx()
        val x = ((containerWidth - popupSize.width) / 2).coerceAtLeast(0)
        val maxY = containerHeight - popupSize.height - verticalMargin
        val y = (anchorY - popupSize.height - verticalMargin).coerceIn(verticalMargin, maxY.coerceAtLeast(verticalMargin))
        IntOffset(x = x, y = y)
    }
}
