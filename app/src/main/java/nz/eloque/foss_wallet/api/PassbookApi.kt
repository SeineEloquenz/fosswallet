package nz.eloque.foss_wallet.api

import android.util.Log
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.PassLoadResult
import nz.eloque.foss_wallet.persistence.PassLoader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

@Suppress("RedundantSuspendModifier")
object PassbookApi {

    private const val TAG = "PassbookApi"
    private const val API_VERSION = "v1"

    suspend fun getUpdated(pass: Pass): PassLoadResult? {
        val requestUrl = "${pass.webServiceUrl}/${API_VERSION}/passes/${pass.passTypeIdentifier}/${pass.serialNumber}"
        val authHeader = Pair("Authorization", "ApplePass ${pass.authToken}")

        val client = OkHttpClient.Builder().build()

        val response = try {
            client.get(requestUrl, authHeader)
        } catch (e: IOException) {
            Log.i(TAG, "Failed to connect to pass api at $requestUrl", e)
            return null
        }
        return if (response.isSuccessful) {
            PassLoader(PassParser()).load(response.body!!.byteStream(), pass.addedAt)
        } else {
            null
        }
    }

    private suspend fun OkHttpClient.get(url: String, vararg headers: Pair<String, String>): Response {
        val requestBuilder = Request.Builder()
            .url(url)
            .get()
        headers.forEach { requestBuilder.header(it.first, it.second) }
        return this.newCall(requestBuilder.build()).execute()
    }
}