package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Tag

@Composable
fun TagCreator(
    onCreate: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        val background = MaterialTheme.colorScheme.onSurface

        var label by remember { mutableStateOf("") }
        var color by remember { mutableStateOf(background) }

        OutlinedTextField(
            label = { Text(stringResource(R.string.tag_label)) },
            value = label,
            onValueChange = { label = it }
        )

        Button(onClick = {
            val tag = Tag(label, color)
            onCreate(tag)
        }) {
            Text(text = stringResource(R.string.add_tag))
        }
    }
}