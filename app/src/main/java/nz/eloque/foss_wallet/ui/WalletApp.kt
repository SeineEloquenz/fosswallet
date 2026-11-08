@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import nz.eloque.compose_kit.navigation.slideBackward
import nz.eloque.compose_kit.navigation.slideForward
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.screens.LibrariesScreen
import nz.eloque.foss_wallet.ui.screens.UpdateFailureScreen
import nz.eloque.foss_wallet.ui.screens.about.AboutScreen
import nz.eloque.foss_wallet.ui.screens.archive.ArchiveScreen
import nz.eloque.foss_wallet.ui.screens.create.AdvancedAddScreen
import nz.eloque.foss_wallet.ui.screens.create.CreateScreen
import nz.eloque.foss_wallet.ui.screens.pass.PassScreen
import nz.eloque.foss_wallet.ui.screens.pass.PassViewModel
import nz.eloque.foss_wallet.ui.screens.scan.ScanScreen
import nz.eloque.foss_wallet.ui.screens.scan.ScanViewModel
import nz.eloque.foss_wallet.ui.screens.settings.SettingsScreen
import nz.eloque.foss_wallet.ui.screens.settings.SettingsViewModel
import nz.eloque.foss_wallet.ui.screens.wallet.WalletScreen
import nz.eloque.foss_wallet.ui.screens.webview.WebviewScreen

@Composable
fun WalletApp(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    passViewModel: PassViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(),
) {
    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    val onOpenPass: (String) -> Unit = { backStack.add(Route.Pass(it)) }
    val popToWallet: () -> Unit = { while (backStack.size > 1) backStack.removeLastOrNull() }
    val replaceTop: (Route) -> Unit = { backStack[backStack.lastIndex] = it }
    val scanScreen: @Composable (BarCode?) -> Unit = { initialBarcode ->
        ScanScreen(
            scanViewModel = scanViewModel,
            onBack = onBack,
            onScanned = { replaceTop(Route.ScanResult(it)) },
            onCancelled = popToWallet,
            onPassCreated = { replaceTop(Route.Pass(it)) },
            onOpenUrl = { backStack.add(Route.Webview(it)) },
            onManualEntry = { backStack.add(Route.Create(it)) },
            initialBarcode = initialBarcode,
        )
    }

    Surface(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            transitionSpec = { slideForward() },
            popTransitionSpec = { slideBackward() },
            predictivePopTransitionSpec = { slideBackward() },
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider =
                entryProvider {
                    entry<Route.Wallet> {
                        WalletScreen(
                            onOpenPass = onOpenPass,
                            onOpenArchive = { backStack.add(Route.Archive) },
                            onOpenSettings = { backStack.add(Route.Settings) },
                            onOpenAdvancedAdd = { backStack.add(Route.AdvancedAdd) },
                            onOpenScan = { backStack.add(Route.Scan) },
                        )
                    }
                    entry<Route.Scan> { scanScreen(null) }
                    entry<Route.ScanResult> { key -> scanScreen(key.toBarCode()) }
                    entry<Route.Archive> {
                        ArchiveScreen(
                            onBack = onBack,
                            onOpenPass = onOpenPass,
                            onOpenSettings = { backStack.add(Route.Settings) },
                        )
                    }
                    entry<Route.About> {
                        AboutScreen(onBack = onBack, onOpenLibraries = { backStack.add(Route.Libraries) })
                    }
                    entry<Route.Webview> { key ->
                        WebviewScreen(url = key.url, onBack = onBack, onOpenPass = onOpenPass)
                    }
                    entry<Route.Settings> {
                        SettingsScreen(
                            onBack = onBack,
                            onOpenAbout = { backStack.add(Route.About) },
                            settingsViewModel = settingsViewModel,
                        )
                    }
                    entry<Route.Libraries> {
                        LibrariesScreen(onBack = onBack)
                    }
                    entry<Route.Create> { key ->
                        CreateScreen(
                            initialBarcode = key.toBarCode(),
                            onBack = onBack,
                            onScanCancelled = popToWallet,
                            onPassCreated = {
                                popToWallet()
                                onOpenPass(it)
                            },
                        )
                    }
                    entry<Route.AdvancedAdd> {
                        AdvancedAddScreen(
                            onBack = onBack,
                            onOpenUrl = { backStack.add(Route.Webview(it)) },
                            onCreate = { backStack.add(it?.let(Route::Create) ?: Route.Create()) },
                        )
                    }
                    entry<Route.Pass> { key ->
                        PassScreen(
                            passId = key.passId,
                            passViewModel = passViewModel,
                            onBack = onBack,
                            onShowUpdateFailure = { reason, rationale -> backStack.add(Route.UpdateFailure(reason, rationale)) },
                        )
                    }
                    entry<Route.UpdateFailure> { key ->
                        UpdateFailureScreen(key.reason, key.rationale, onBack = onBack)
                    }
                },
        )
    }
}
