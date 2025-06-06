package nz.eloque.foss_wallet.ui.screens

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.model.GroupWithPasses
import nz.eloque.foss_wallet.ui.wallet.PassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupId: Long,
    initialIndex: Int,
    navController: NavHostController,
    passViewModel: PassViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val group = remember { mutableStateOf(GroupWithPasses.placeholder())}
    LaunchedEffect(coroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            group.value = passViewModel.groupById(groupId)
        }
    }
    val passes = group.value.passes

    val pagerState = rememberPagerState(initialIndex) { passes.size }

    HorizontalPager(
        state = pagerState,
    ) { index ->
        val item = passes[index]
        PassScreen(item.id, navController, passViewModel)

    }
}