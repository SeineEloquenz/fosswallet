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
    suspend fun getUpdated(pass: Pass): UpdateResult {
        val first = fetch(pass)
        // Some issuers gate the pass behind device registration and answer 204 until the device is registered.
        return if (first is FetchOutcome.NeedsRegistration) {
            if (register(pass)) {
                when (val retry = fetch(pass)) {
                    is FetchOutcome.Result -> retry.result
                    is FetchOutcome.NeedsRegistration -> UpdateResult.NotUpdated
                }
            } else {
                UpdateResult.NotUpdated
            }
        } else {
            (first as FetchOutcome.Result).result
        }
    }

    /**
     * Registers this device for the given pass using the PassKit web service protocol.
     */
    suspend fun register(pass: Pass): Boolean {
        val webServiceUrl = pass.webServiceUrl?.trimEnd('/') ?: return false
        val requestUrl =
            "$webServiceUrl/$API_VERSION/devices/${pass.deviceId}" +
                "/registrations/${pass.passTypeIdentifier}/${pass.serialNumber}"
        val authHeader = Pair("Authorization", "ApplePass ${pass.authToken}")
        val body = JSONObject().put("pushToken", pass.deviceId.toString()).toString()

        val response =
            try {
                client.post(requestUrl, body, authHeader)
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

    private suspend fun fetch(pass: Pass): FetchOutcome {
        val webServiceUrl = pass.webServiceUrl!!.trimEnd('/')
        val requestUrl = "$webServiceUrl/$API_VERSION/passes/${pass.passTypeIdentifier}/${pass.serialNumber}"
        val authHeader = Pair("Authorization", "ApplePass ${pass.authToken}")

        val response =
            try {
                client.get(requestUrl, authHeader)
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
                            FetchOutcome.Result(
                                UpdateResult.Success(UpdateContent.LoadResult(PassLoader(PassParser()).load(body, pass.id, pass.addedAt))),
                            )
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
