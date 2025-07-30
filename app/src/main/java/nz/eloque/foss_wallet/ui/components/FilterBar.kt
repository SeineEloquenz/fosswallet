package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    SearchBar(
        windowInsets = WindowInsets(0.dp),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                placeholder = { Text(stringResource(R.string.search)) },
                onQueryChange = {
                    query = it
                    onSearch.invoke(it)
                },
                onSearch = {
                    focusManager.clearFocus()
                },
                onExpandedChange = {},
                expanded = false,
                modifier = Modifier.fillMaxHeight(),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                query = ""
                                onSearch.invoke("")
                            },
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier
    ) {

    }
}

@Preview
@Composable
private fun FilterBarPreview() {
    FilterBar({})
}
