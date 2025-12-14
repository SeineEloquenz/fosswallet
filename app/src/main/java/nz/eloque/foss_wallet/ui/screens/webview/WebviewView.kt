package nz.eloque.foss_wallet.ui.screens.webview

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.persistence.loader.LoaderResult
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayInputStream


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebviewView(
    navController: NavHostController,
    passViewModel: PassViewModel,
    url: String,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidView(factory = {
        val webview = WebView(it)
        webview.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient =
                CustomWebViewClient(context, passViewModel, coroutineScope, navController)
            loadUrl(url)
        }

        webview.settings.userAgentString =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Mobile/15E148 Safari/604.1";
        webview.settings.javaScriptEnabled = true
        webview
    }, update = {
        it.loadUrl(url)
    })
}

class CustomWebViewClient(
    val context: Context,
    val passViewModel: PassViewModel,
    val coroutineScope: CoroutineScope,
    val navController: NavController
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return interceptRequest(view, request)
    }

    private fun interceptRequest(
        webView: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return try {
            val okhttp: OkHttpClient = OkHttpClient.Builder().build()
            val okHttpRequest = Request.Builder().also {
                it.url(request?.url.toString())
                for (header in request!!.requestHeaders) {
                    if (header.key.startsWith("sec-ch-ua")) continue;
                    it.addHeader(header.key, header.value)
                }
            }
            val response = okhttp.newCall(okHttpRequest.build()).execute()

            val contentType = response.headers["content-type"]?.split(";")?.first()

            if (contentType == "application/vnd.apple.pkpass") {
                return handlePkPassResponse(response)
            } else {
                WebResourceResponse(contentType, "UTF-8", response.body.byteStream(), )
            }
        } catch (_: Exception) {
            super.shouldInterceptRequest(webView, request)
        }
    }

    private fun handlePkPassResponse(response: Response): WebResourceResponse {
        val bytes = response.body.byteStream().readBytes();
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val result = Loader(context).handleInputStream(
                    ByteArrayInputStream(bytes),
                    passViewModel,
                    coroutineScope
                )
                if (result is LoaderResult.Single) {
                    withContext(Dispatchers.Main) {
                        navController.popBackStack()
                        navController.navigate("pass/${result.passId}")
                    }
                }
            }
        }

        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()));
    }
}