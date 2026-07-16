package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import nz.eloque.compose_kit.input.SimpleTextField
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Tag

val INITIAL_ENVELOPE: ColorEnvelope = ColorEnvelope(Color.White, "ffffffff", false)

@Composable
fun TagCreator(
    onCreate: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    var label by remember { mutableStateOf("") }
    var colorEnvelope by remember { mutableStateOf(INITIAL_ENVELOPE) }
    var hexInput by remember { mutableStateOf("ffffff") }

    val controller = rememberColorPickerController()

    val hexRegex = Regex("^[0-9a-f]{6}$", RegexOption.IGNORE_CASE)
    val isHexValid = hexInput.matches(hexRegex) && listOf(0, 2, 4).any { hexInput.substring(it, it + 2) == "ff" }
    val isLabelValid = label.isNotEmpty() && label.length < 30

    LaunchedEffect(colorEnvelope) {
        hexInput = colorEnvelope.hexCode.drop(2)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .width(308.dp),
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.tag_label)) },
            isError = !isLabelValid,
            value = label,
            onValueChange = { label = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HsvColorPicker(
                controller = controller,
                initialColor = colorEnvelope.color,
                onColorChanged = { colorEnvelope = it },
                modifier =
                    Modifier
                        .width(150.dp)
                        .height(150.dp),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.height(150.dp),
            ) {
                AlphaTile(
                    modifier =
                        Modifier
                            .weight(1f)
                            .width(150.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    controller = controller,
                )

                Spacer(modifier = Modifier.height(8.dp))

                SimpleTextField(
                    imageVector = Icons.Default.Tag,
                    onSubmit = {},
                    value = hexInput,
                    onValueChange = {
                        hexInput = it
                        if (it.matches(hexRegex)) {
                            val color =
                                Color(
                                    red = it.substring(0, 2).toInt(16) / 255f,
                                    green = it.substring(2, 4).toInt(16) / 255f,
                                    blue = it.substring(4, 6).toInt(16) / 255f,
                                )
                            controller.selectByColor(color, true)
                        }
                    },
                    isError = !isHexValid,
                    modifier = Modifier.width(150.dp),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                )
            }
        }

        Button(
            onClick = {
                val tag = Tag(label.trim(), colorEnvelope.color)
                onCreate(tag)
            },
            enabled = isHexValid && isLabelValid,
        ) {
            Text(text = stringResource(R.string.add_tag))
        }
    }
}