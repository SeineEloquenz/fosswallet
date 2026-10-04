package nz.eloque.foss_wallet.api

import android.util.Log
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.loader.InvalidPassException
import nz.eloque.foss_wallet.persistence.loader.PassLoader
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.net.SocketTimeoutException

@Suppress("RedundantSuspendModifier")
class PassbookApi(
    private val client: OkHttpClient = OkHttpClient.Builder().build(),
) {
    suspend fun getUpdated(
        pass: Pass,
        registered: Boolean = false,
        onRegistered: suspend () -> Unit = {},
    ): UpdateResult =
        when (val first = fetch(pass)) {
            is FetchOutcome.Result -> first.result
            // Some issuers gate the pass behind device registration and answer 204 until the device is registered.
            is FetchOutcome.NeedsRegistration ->
                if (registered || !register(pass)) {
                    UpdateResult.NotUpdated
                } else {
                    onRegistered()
                    when (val retry = fetch(pass)) {
                        is FetchOutcome.Result -> retry.result
                        is FetchOutcome.NeedsRegistration -> UpdateResult.NotUpdated
                    }
                }
        }

    /**
     * Registers this device for the given pass using the PassKit web service protocol.
     */
    suspend fun register(pass: Pass): Boolean {
        val requestUrl = pass.registrationUrl() ?: return false
        // There is no APNs token, issuers that validate the push token format will reject the registration.
        val body = JSONObject().put("pushToken", pass.deviceId.toString()).toString()

        val response =
            try {
                client.post(requestUrl, body, pass.authHeader())
            } catch (e: SocketTimeoutException) {
                Log.i(TAG, "Timeout while registering device at $requestUrl", e)
                return false
            } catch (e: IOException) {
                Log.i(TAG, "Failed to register device at $requestUrl", e)
                return false
            }
        return response.use {
            when (it.code) {
                200, 201 -> true
                else -> {
                    Log.i(TAG, "Device registration at $requestUrl returned ${it.code}")
                    false
                }
            }
        }
    }

    /**
     * Unregisters this device for the given pass using the PassKit web service protocol.
     */
    suspend fun unregister(pass: Pass): Boolean {
        val requestUrl = pass.registrationUrl() ?: return false
        val response =
            try {
                client.delete(requestUrl, pass.authHeader())
            } catch (e: IOException) {
                Log.i(TAG, "Failed to unregister device at $requestUrl", e)
                return false
            }
        return response.use { it.code == 200 }
    }

    private fun Pass.registrationUrl(): String? =
        webServiceUrl?.trimEnd('/')?.let {
            "$it/$API_VERSION/devices/$deviceId/registrations/$passTypeIdentifier/$serialNumber"
        }

    private fun Pass.authHeader(): Pair<String, String> = Pair("Authorization", "ApplePass $authToken")

    private suspend fun fetch(pass: Pass): FetchOutcome {
        val webServiceUrl = pass.webServiceUrl!!.trimEnd('/')
        val requestUrl = "$webServiceUrl/$API_VERSION/passes/${pass.passTypeIdentifier}/${pass.serialNumber}"
        val response =
            try {
                client.get(requestUrl, pass.authHeader())
            } catch (e: SocketTimeoutException) {
                Log.i(TAG, "Timeout while connecting to pass api at $requestUrl", e)
                return FetchOutcome.Result(UpdateResult.Failed(FailureReason.Timeout))
            } catch (e: IOException) {
                Log.i(TAG, "Failed to connect to pass api at $requestUrl", e)
                return FetchOutcome.Result(UpdateResult.Failed(FailureReason.Exception(e)))
            }
        return response.use {
            when (it.code) {
                204 -> FetchOutcome.NeedsRegistration
                200 -> {
                    val body = it.body.bytes()
                    if (body.isEmpty()) {
                        FetchOutcome.Result(UpdateResult.NotUpdated)
                    } else {
                        try {
                            val loadResult = PassLoader(PassParser()).load(body, pass.id, pass.addedAt, pass.deviceId)
                            FetchOutcome.Result(UpdateResult.Success(UpdateContent.LoadResult(loadResult)))
                        } catch (e: InvalidPassException) {
                            FetchOutcome.Result(UpdateResult.Failed(FailureReason.Exception(e)))
                        }
                    }
                }
                304 -> FetchOutcome.Result(UpdateResult.NotUpdated)
                403 -> FetchOutcome.Result(UpdateResult.Failed(FailureReason.Forbidden))
                else -> FetchOutcome.Result(UpdateResult.Failed(FailureReason.Status(it.code)))
            }
        }
    }

    private sealed interface FetchOutcome {
        data class Result(
            val result: UpdateResult,
        ) : FetchOutcome

        data object NeedsRegistration : FetchOutcome
    }

    private suspend fun OkHttpClient.get(
        url: String,
        vararg headers: Pair<String, String>,
    ): Response {
        val requestBuilder =
            Request
                .Builder()
                .url(url)
                .get()
        headers.forEach { requestBuilder.header(it.first, it.second) }
        return this.newCall(requestBuilder.build()).execute()
    }

    private suspend fun OkHttpClient.delete(
        url: String,
        vararg headers: Pair<String, String>,
    ): Response {
        val requestBuilder =
            Request
                .Builder()
                .url(url)
                .delete()
        headers.forEach { requestBuilder.header(it.first, it.second) }
        return this.newCall(requestBuilder.build()).execute()
    }

    private suspend fun OkHttpClient.post(
        url: String,
        json: String,
        vararg headers: Pair<String, String>,
    ): Response {
        val requestBuilder =
            Request
                .Builder()
                .url(url)
                .post(json.toRequestBody(JSON_MEDIA_TYPE))
        headers.forEach { requestBuilder.header(it.first, it.second) }
        return this.newCall(requestBuilder.build()).execute()
    }

    companion object {
        private const val TAG = "PassbookApi"
        private const val API_VERSION = "v1"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
