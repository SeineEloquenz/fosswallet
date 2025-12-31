package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Tag

@Composable
fun TagCreator(
    onCreate: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = rememberColorPickerController()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        var label by remember { mutableStateOf("") }
        var color by remember { mutableStateOf(Color.White) }

        OutlinedTextField(
            label = { Text(stringResource(R.string.tag_label)) },
            value = label,
            onValueChange = { label = it }
        )

        HsvColorPicker(
            controller = controller,
            initialColor = color,
            onColorChanged = { color = it.color },
            modifier = Modifier.fillMaxWidth()
                .height(150.dp)
        )

        Button(onClick = {
            val tag = Tag(label, color)
            onCreate(tag)
        }) {
            Text(text = stringResource(R.string.add_tag))
        }
    }
}