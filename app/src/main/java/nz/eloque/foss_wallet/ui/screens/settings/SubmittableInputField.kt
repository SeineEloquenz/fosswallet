package nz.eloque.foss_wallet.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SubmittableTextField(
    label: @Composable () -> Unit,
    imageVector: ImageVector,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    inputValidator: (String) -> Boolean = { true },
    initialValue: String = "",
    clearOnSubmit: Boolean = true,
    contentDescription: String = "",
) {
    val msgInput = rememberSaveable { mutableStateOf(initialValue) }

    val onlyWhitespace: () -> Boolean = { msgInput.value.trim() == "" }
    val validInput: () -> Boolean = { !onlyWhitespace() && inputValidator.invoke(msgInput.value) }

    val isError = rememberSaveable { mutableStateOf( !validInput() ) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = msgInput.value,
            label = label,
            singleLine = singleLine,
            isError = isError.value,
            modifier = modifier.fillMaxWidth(0.8f),
            onValueChange = {
                msgInput.value = it
                isError.value = !validInput()
                onValueChange.invoke(it)
            },
            enabled = enabled,
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
            enabled = enabled && !isError.value,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
            )
        }
    }
}