package nz.eloque.foss_wallet.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import nz.eloque.foss_wallet.model.Attachment
import nz.eloque.foss_wallet.utils.getMimeType

@Composable
fun AttachmentListEntry(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val openAttachment = {
        val attachmentFile = attachment.getFile(context)
        val mimeType = attachmentFile.getMimeType()
        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                attachmentFile,
            )

        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

        context.startActivity(Intent.createChooser(intent, "Open file"))
    }

    ElevatedCard(
        modifier =
            modifier
                .clickable(
                    onClick = openAttachment,
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(8.dp),
        ) {
            Text(
                text = attachment.fileName,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
        }
    }
}

@Composable
fun AttachmentList(
    attachments: List<Attachment>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        attachments.forEach { attachment ->
            AttachmentListEntry(
                attachment = attachment,
            )
        }
    }
}
