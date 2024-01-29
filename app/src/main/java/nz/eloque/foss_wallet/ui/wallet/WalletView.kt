package nz.eloque.foss_wallet.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import nz.eloque.foss_wallet.ui.components.LabelledCheckBox
import java.util.UUID


data class TaskItem(
    val uuid: UUID,
    val checked: Boolean,
    val content: String
)
@Composable
fun WalletView(
    modifier: Modifier = Modifier
) {
    val _tasks = remember { MutableStateFlow(listOf<TaskItem>()) }
    val tasks by remember { _tasks }.collectAsState()
    val state = rememberLazyListState()
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            state = state,
            modifier = modifier
                .fillMaxWidth()
                .weight(9f)
        ) {
            itemsIndexed(tasks) { index, task ->
                val checked = remember { mutableStateOf(false) }
                LabelledCheckBox(
                    checked = checked.value,
                    onCheckedChange = {
                        checked.value = !checked.value
                        val newList = ArrayList(tasks)
                        newList[index] = newList[index].copy(checked = checked.value)
                        _tasks.value = newList
                    },
                    label = task.content,
                    modifier = modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}