package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Tag

val INITIAL_ENVELOPE: ColorEnvelope = ColorEnvelope(Color.White, "ffffffff", false)

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
        var colorEnvelope by remember { mutableStateOf(INITIAL_ENVELOPE) }


        val valid = !label.isEmpty() && label.length < 30

        OutlinedTextField(
            label = { Text(stringResource(R.string.tag_label)) },
            isError = !valid,
            value = label,
            onValueChange = { label = it }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            HsvColorPicker(
                controller = controller,
                initialColor = colorEnvelope.color,
                onColorChanged = { colorEnvelope = it },
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AlphaTile(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    controller = controller,
                )

                Text(text = colorEnvelope.let { "#${it.hexCode}" })
            }
        }


        Button(
            onClick = {
                val tag = Tag(label.trim(), colorEnvelope.color)
                onCreate(tag)
            },
            enabled = valid
        ) {
            Text(text = stringResource(R.string.add_tag))
        }
    }
}