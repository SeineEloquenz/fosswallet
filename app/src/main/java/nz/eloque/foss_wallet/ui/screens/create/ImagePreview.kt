package nz.eloque.foss_wallet.ui.screens.create

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R

@Composable
fun ImagePreview(
    uri: Uri,
    mimeType: String?,
    scanningEnabled: Boolean,
    onScanned: (FileScanner.ScanResult) -> Unit,
    onPickFile: () -> Unit,
) {
    val resolver = LocalContext.current.contentResolver
    var pageIndex by remember(uri) { mutableIntStateOf(0) }
    var page by remember(uri, pageIndex) { mutableStateOf<PreviewPage?>(null) }
    var pageCount by remember(uri) { mutableIntStateOf(1) }
    var failed by remember(uri, pageIndex) { mutableStateOf(false) }
    var zoom by remember(uri, pageIndex) { mutableFloatStateOf(1f) }
    var pan by remember(uri, pageIndex) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var match by remember(uri, pageIndex) { mutableStateOf<PreviewMatch?>(null) }
    var notFound by remember(uri, pageIndex) { mutableStateOf(false) }

    LaunchedEffect(uri, pageIndex) {
        try {
            val loaded = withContext(Dispatchers.IO) { loadPreviewPage(resolver, uri, pageIndex, mimeType) }
            pageCount = loaded.pageCount
            page = loaded
        } catch (_: java.io.IOException) {
            failed = true
        } catch (_: OutOfMemoryError) {
            failed = true
        } catch (_: SecurityException) {
            failed = true
        } catch (_: IllegalArgumentException) {
            failed = true
        }
    }
    val bitmap = page?.bitmap
    LaunchedEffect(bitmap, viewportSize, zoom, pan, scanningEnabled) {
        if (!scanningEnabled || bitmap == null || viewportSize.width == 0 || viewportSize.height == 0) return@LaunchedEffect
        delay(350)
        val viewport = PreviewViewport(bitmap.width, bitmap.height, viewportSize.width, viewportSize.height, zoom, pan.x, pan.y)
        val bounds = viewport.visibleBounds()
        val detection =
            bounds?.let {
                withContext(Dispatchers.Default) {
                    val crop = Bitmap.createBitmap(bitmap, bounds.x, bounds.y, bounds.width, bounds.height)
                    try {
                        FileScanner.detect(crop)
                    } finally {
                        if (crop !== bitmap) crop.recycle()
                    }
                }
            }
        match =
            detection?.let { found ->
                PreviewMatch(found.result, found.corners.map { Offset((it.x + bounds.x).toFloat(), (it.y + bounds.y).toFloat()) })
            }
        notFound = detection == null
    }
    val outlineColor = MaterialTheme.colorScheme.primary

    Box(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(uri, pageIndex) {
                        detectTransformGestures { centroid, change, factor, _ ->
                            val image = page?.bitmap ?: return@detectTransformGestures
                            val nextViewport =
                                PreviewViewport(image.width, image.height, size.width, size.height, zoom, pan.x, pan.y)
                                    .transformed(centroid.x, centroid.y, change.x, change.y, factor)
                            pan = Offset(nextViewport.panX, nextViewport.panY)
                            zoom = nextViewport.zoom
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    val image = remember(bitmap) { bitmap.asImageBitmap() }
                    Canvas(Modifier.fillMaxSize()) {
                        val viewport =
                            PreviewViewport(bitmap.width, bitmap.height, size.width.toInt(), size.height.toInt(), zoom, pan.x, pan.y)
                        withTransform({
                            translate(viewport.left, viewport.top)
                            scale(viewport.scale, viewport.scale, Offset.Zero)
                        }) {
                            drawImage(image)
                            match?.let {
                                val outline =
                                    Path().apply {
                                        moveTo(it.corners[0].x, it.corners[0].y)
                                        it.corners.drop(1).forEach { corner -> lineTo(corner.x, corner.y) }
                                        close()
                                    }
                                drawPath(outline, outlineColor, style = Stroke(width = 4.dp.toPx() / viewport.scale))
                            }
                        }
                    }
                    ScannerOverlay(modifier = Modifier.fillMaxSize())
                } else if (failed) {
                    Text(stringResource(R.string.preview_load_failed), color = Color.White, modifier = Modifier.padding(24.dp))
                } else {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            if (pageCount > 1) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    IconButton(onClick = { pageIndex-- }, enabled = pageIndex > 0) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            stringResource(R.string.previous_page),
                            tint = Color.White.copy(alpha = if (pageIndex > 0) 1f else 0.38f),
                        )
                    }
                    Text("${pageIndex + 1} / $pageCount", color = Color.White)
                    IconButton(onClick = { pageIndex++ }, enabled = pageIndex + 1 < pageCount) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            stringResource(R.string.next_page),
                            tint = Color.White.copy(alpha = if (pageIndex + 1 < pageCount) 1f else 0.38f),
                        )
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .padding(horizontal = 88.dp),
                contentAlignment = Alignment.Center,
            ) {
                val found = match
                if (found != null) {
                    Button(onClick = { onScanned(found.result) }) { Text(stringResource(R.string.use_this_barcode)) }
                } else if (notFound) {
                    val hint = if (pageCount > 1) R.string.no_barcode_on_page else R.string.no_barcode_in_view
                    Text(stringResource(hint), color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
        FilePickerButton(
            onClick = onPickFile,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@Composable
internal fun FilePickerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val description = stringResource(R.string.choose_another_file)
    Box(
        modifier
            .size(56.dp)
            .clip(shape)
            .background(Color(0xFF252525))
            .border(2.dp, Color.White, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White)
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(22.dp)
                .background(Color(0xFF252525), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

private data class PreviewMatch(
    val result: FileScanner.ScanResult,
    val corners: List<Offset>,
)
