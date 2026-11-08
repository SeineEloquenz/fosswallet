package nz.eloque.foss_wallet.ui.screens.webview

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import nz.eloque.foss_wallet.ui.ImportEventsEffect
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.utils.PkpassMimeTypes
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebviewView(
    walletViewModel: WalletViewModel,
    url: String,
    onImported: () -> Unit,
    onOpenPass: (passId: String) -> Unit,
) {
    ImportEventsEffect(walletViewModel, onOpenPass, onImported = onImported)

    AndroidView(factory = {
        val webview = WebView(it)
        webview.apply {
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            webViewClient =
                CustomWebViewClient(walletViewModel)
            loadUrl(url)
        }

        webview.settings.userAgentString =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Mobile/15E148 Safari/604.1"
        webview.settings.javaScriptEnabled = true
        webview
    }, update = {
        it.loadUrl(url)
    })
}

class CustomWebViewClient(
    val walletViewModel: WalletViewModel,
) : WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?,
    ): WebResourceResponse? = interceptRequest(view, request)

    private val client = HttpClient(OkHttp)

    private fun interceptRequest(
        webView: WebView?,
        request: WebResourceRequest?,
    ): WebResourceResponse? =
        try {
            val (contentType, body) =
                runBlocking {
                    val response =
                        client.get(request!!.url.toString()) {
                            for (header in request.requestHeaders) {
                                if (header.key.startsWith("sec-ch-ua") || HttpHeaders.isUnsafe(header.key)) continue
                                header(header.key, header.value)
                            }
                        }
                    response.headers[HttpHeaders.ContentType]?.split(";")?.first() to response.readRawBytes()
                }

            if (PkpassMimeTypes.contains(contentType)) {
                handlePkPassResponse(body)
            } else {
                WebResourceResponse(contentType, "UTF-8", ByteArrayInputStream(body))
            }
        } catch (_: Exception) {
            super.shouldInterceptRequest(webView, request)
        }

    private fun handlePkPassResponse(body: ByteArray): WebResourceResponse {
        walletViewModel.import(body)
        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
    }
}
