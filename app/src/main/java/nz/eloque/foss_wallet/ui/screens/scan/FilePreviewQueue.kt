package nz.eloque.foss_wallet.ui.screens.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.ui.Route

/** Previews queued files one at a time, each scanned barcode replacing the previous scan result. */
@Composable
fun rememberFilePreviewQueue(navController: NavHostController): (List<FilePreviewInput>) -> Unit {
    var queue by rememberSaveable(stateSaver = FilePreviewQueueSaver) { mutableStateOf(emptyList<FilePreviewInput>()) }
    var previewOpen by rememberSaveable { mutableStateOf(false) }
    val launcher =
        rememberLauncherForActivityResult(FilePreviewContract()) { barcode ->
            previewOpen = false
            queue = queue.drop(1)
            if (barcode != null) {
                navController.navigate(Route.ScanResult(barcode)) {
                    popUpTo<Route.ScanResult> { inclusive = true }
                }
            }
        }
    LaunchedEffect(queue, previewOpen) {
        if (previewOpen) return@LaunchedEffect
        queue.firstOrNull()?.let {
            previewOpen = true
            launcher.launch(it)
        }
    }
    return remember { { files -> queue = queue + files } }
}

private val FilePreviewQueueSaver =
    listSaver<List<FilePreviewInput>, String?>(
        save = { queue -> queue.flatMap { listOf(it.uri.toString(), it.mimeType) } },
        restore = { saved -> saved.chunked(2).map { (uri, mimeType) -> FilePreviewInput(Uri.parse(uri), mimeType) } },
    )
