package nz.eloque.foss_wallet.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun SubmittableTextField(
    label: String,
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
    var text by rememberSaveable { mutableStateOf(initialValue) }

    val isError by remember { derivedStateOf {
        val trimmed = text.trim()
        trimmed.isEmpty() || !inputValidator(trimmed)
    } }

    val errorMessage = if (isError && text.isNotBlank()) "Invalid input" else null
    val buttonEnabled = enabled && !isError && text != initialValue

    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            label = { Text(text = label) },
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
            },
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (!isError) {
                            onSubmit(text)
                            if (clearOnSubmit) text = ""
                        }
                    },
                    enabled = buttonEnabled
                ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = contentDescription,
                        tint = if (buttonEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!isError) {
                        onSubmit(text)
                        if (clearOnSubmit) text = ""
                    }
                }
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}