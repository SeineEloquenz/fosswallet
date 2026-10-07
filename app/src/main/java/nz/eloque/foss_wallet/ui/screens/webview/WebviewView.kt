package nz.eloque.foss_wallet.ui.screens.webview

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.ui.ImportEventsEffect
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.utils.PkpassMimeTypes
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebviewView(
    navController: NavHostController,
    walletViewModel: WalletViewModel,
    url: String,
) {
    ImportEventsEffect(walletViewModel, navController, onImported = { navController.popBackStack() })

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

    private fun interceptRequest(
        webView: WebView?,
        request: WebResourceRequest?,
    ): WebResourceResponse? {
        return try {
            val okhttp: OkHttpClient = OkHttpClient.Builder().build()
            val okHttpRequest =
                Request.Builder().also {
                    it.url(request?.url.toString())
                    for (header in request!!.requestHeaders) {
                        if (header.key.startsWith("sec-ch-ua")) continue
                        it.addHeader(header.key, header.value)
                    }
                }
            val response = okhttp.newCall(okHttpRequest.build()).execute()

            val contentType = response.headers["content-type"]?.split(";")?.first()

            if (PkpassMimeTypes.contains(contentType)) {
                return handlePkPassResponse(response)
            } else {
                WebResourceResponse(contentType, "UTF-8", response.body.byteStream())
            }
        } catch (_: Exception) {
            super.shouldInterceptRequest(webView, request)
        }
    }

    private fun handlePkPassResponse(response: Response): WebResourceResponse {
        walletViewModel.import(response.body.byteStream().readBytes())
        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
    }
}
