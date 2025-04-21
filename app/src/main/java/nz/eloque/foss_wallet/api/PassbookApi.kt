package nz.eloque.foss_wallet.api

import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.PassBitmaps
import nz.eloque.foss_wallet.persistence.PassLoader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

@Suppress("RedundantSuspendModifier")
object PassbookApi {

    private const val API_VERSION = "v1"

    suspend fun getUpdated(pass: Pass): Triple<Pass, PassBitmaps, Set<PassLocalization>>? {
        val requestUrl = "${pass.webServiceUrl}/${API_VERSION}/passes/${pass.passTypeIdentifier}/${pass.serialNumber}"
        val authHeader = Pair("Authorization", "ApplePass ${pass.authToken}")

        val client = OkHttpClient.Builder().build()

        val response = client.get(requestUrl, authHeader)
        return if (response.isSuccessful) {
            PassLoader(PassParser()).load(response.body!!.byteStream())
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