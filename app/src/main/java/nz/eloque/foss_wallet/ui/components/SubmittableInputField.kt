package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector


@Composable
fun SubmittableTextField(
    label: @Composable () -> Unit,
    imageVector: ImageVector,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    inputValidator: (String) -> Boolean = { true },
    initialValue: String = "",
    clearOnSubmit: Boolean = true,
    contentDescription: String = "",
) {
    val msgInput = rememberSaveable { mutableStateOf(initialValue) }

    val onlyWhitespace: () -> Boolean = { msgInput.value.trim() == "" }
    val validInput: () -> Boolean = { !onlyWhitespace() && inputValidator.invoke(msgInput.value) }

    val isError = rememberSaveable { mutableStateOf( !validInput() ) }
    BoxWithConstraints(
        modifier = modifier,
    ) {
        val maxWidth = maxWidth
        Row {
            OutlinedTextField(
                value = msgInput.value,
                label = label,
                singleLine = true,
                isError = isError.value,
                modifier = modifier.width(maxWidth * 0.8f),
                onValueChange = {
                    msgInput.value = it
                    isError.value = !validInput()
                    onValueChange.invoke(it)
                }
            )
            IconButton(
                onClick = {
                    if (validInput()) {
                        onSubmit.invoke(msgInput.value)
                        if (clearOnSubmit) {
                            msgInput.value = ""
                        }
                        isError.value = !validInput()
                    }
                },
                enabled = !isError.value,
                modifier = modifier
                    .width(maxWidth * 0.2f)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription
                )
            }
        }
    }
}
