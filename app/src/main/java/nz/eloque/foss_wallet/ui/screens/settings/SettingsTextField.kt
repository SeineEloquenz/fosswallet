package nz.eloque.foss_wallet.ui.screens.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R

@Composable
fun SettingsTextField(
    title: String,
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

    val isError by remember {
        derivedStateOf {
            val trimmed = text.trim()
            trimmed.isEmpty() || !inputValidator(trimmed)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val handleSubmit = {
        if (!isError) {
            onSubmit(text)
            if (clearOnSubmit) text = ""
        }
    }

    Row(
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.6875f),
        )

        TextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
            },
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            modifier = Modifier.weight(.3125f),
            textStyle = MaterialTheme.typography.bodyLarge,
            interactionSource = interactionSource,
            leadingIcon = {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint =
                        if (!enabled) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else if (isError) {
                            MaterialTheme.colorScheme.error
                        } else if (focused) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            },
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { handleSubmit() },
                ),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            supportingText =
                if (isError && text.isNotBlank()) {
                    { Text(stringResource(R.string.invalid_input)) }
                } else {
                    null
                },
        )
    }
}
