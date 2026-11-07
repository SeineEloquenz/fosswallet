package nz.eloque.foss_wallet.ui

import android.app.Activity
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.shortcut.ShortcutService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.Charset

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavigationTest {
    @Test
    fun `multiline barcode opens editor without changing its contents`() {
        assertBarcodeRoundTrip("A\r\nB", "A\r\nB")
    }

    @Test
    fun `reserved characters and Unicode survive navigation`() {
        assertBarcodeRoundTrip("https://example.org/a?x=1&y=2#fragment + %20 / Zürich", "?message=other&altText=other# + %20")
    }

    @Test
    fun `create route without a barcode has no barcode`() {
        withController { controller ->
            controller.navigate(Route.Create())

            assertNull(controller.currentBackStackEntry!!.toRoute<Route.Create>().toBarCode())
        }
    }

    @Test
    fun `missing alternate text remains null`() {
        assertBarcodeRoundTrip("example", null)
    }

    @Test
    fun `empty alternate text remains empty`() {
        assertBarcodeRoundTrip("example", "")
    }

    @Test
    fun `barcode format and non-default encoding survive navigation`() {
        assertBarcodeRoundTrip("Zürich", "Ticket", BarcodeFormat.AZTEC, Charsets.ISO_8859_1)
    }

    @Test
    fun `pinned pass shortcuts open the pass`() {
        withController { controller ->
            controller.navigate("${ShortcutService.BASE_URI}/pass-id".toUri())

            assertEquals(Route.Pass("pass-id"), controller.currentBackStackEntry!!.toRoute<Route.Pass>())
        }
    }

    @Test
    fun `webview url survives navigation`() {
        assertRoundTrip(Route.Webview("https://example.org/a b?x=1&y=2#frag+%20"))
    }

    @Test
    fun `update failure with slashes and a multiline stacktrace survives navigation`() {
        assertRoundTrip(Route.UpdateFailure("Failed /v1/passes/a/b", "java.io.IOException: x/y\n\tat a.b(C.kt:1)\n"))
    }

    private fun assertBarcodeRoundTrip(
        message: String,
        altText: String?,
        format: BarcodeFormat = BarcodeFormat.QR_CODE,
        encoding: Charset = Charsets.UTF_8,
    ) {
        val barcode = BarCode(format, message, encoding, altText)
        withController { controller ->
            controller.navigate(Route.Create(barcode))

            assertEquals(barcode, controller.currentBackStackEntry!!.toRoute<Route.Create>().toBarCode())
        }
    }

    private inline fun <reified T : Route> assertRoundTrip(route: T) {
        withController { controller ->
            controller.navigate(route)

            assertEquals(route, controller.currentBackStackEntry!!.toRoute<T>())
        }
    }

    private fun withController(block: (NavHostController) -> Unit) {
        Robolectric.buildActivity(Activity::class.java).setup().use { activity ->
            val controller = NavHostController(activity.get())
            controller.navigatorProvider.addNavigator(ComposeNavigator())
            controller.graph =
                controller.createGraph(startDestination = Route.Wallet) {
                    composable<Route.Wallet> {}
                    composable<Route.Create> {}
                    composable<Route.Pass>(deepLinks = listOf(navDeepLink<Route.Pass>(basePath = ShortcutService.BASE_URI))) {}
                    composable<Route.Webview> {}
                    composable<Route.UpdateFailure> {}
                }
            block(controller)
        }
    }
}
