package nz.eloque.foss_wallet.api

import android.util.Log
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.loader.InvalidPassException
import nz.eloque.foss_wallet.persistence.loader.PassLoader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.net.SocketTimeoutException

@Suppress("RedundantSuspendModifier")
object PassbookApi {

    private const val TAG = "PassbookApi"
    private const val API_VERSION = "v1"

    suspend fun getUpdated(pass: Pass): UpdateResult {
        val webServiceUrl = pass.webServiceUrl!!.trimEnd('/')
        val requestUrl = "$webServiceUrl/${API_VERSION}/passes/${pass.passTypeIdentifier}/${pass.serialNumber}"
        val authHeader = Pair("Authorization", "ApplePass ${pass.authToken}")

        val client = OkHttpClient.Builder().build()

        val response = try {
            client.get(requestUrl, authHeader)
        } catch (e: SocketTimeoutException) {
            Log.i(TAG, "Timeout while connecting to pass api at $requestUrl", e)
            return UpdateResult.Failed(FailureReason.Timeout)
        }
        catch (e: IOException) {
            Log.i(TAG, "Failed to connect to pass api at $requestUrl", e)
            return UpdateResult.Failed(FailureReason.Exception(e))
        }
        return if (response.isSuccessful) {
            try {
                UpdateResult.Success(UpdateContent.LoadResult(PassLoader(PassParser()).load(response.body.bytes(), pass.id, pass.addedAt)))
            } catch (e: InvalidPassException) {
                return UpdateResult.Failed(FailureReason.Exception(e))
            }
        } else {
            return when (response.code) {
                304 -> UpdateResult.NotUpdated
                403 -> UpdateResult.Failed(FailureReason.Forbidden)
                else -> UpdateResult.Failed(FailureReason.Status(response.code))
            }
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