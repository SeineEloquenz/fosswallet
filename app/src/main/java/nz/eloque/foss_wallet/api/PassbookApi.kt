package nz.eloque.foss_wallet.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.model.PassWebService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.net.SocketTimeoutException

class PassbookApi(
    client: OkHttpClient = OkHttpClient(),
) {
    private val client = client.newBuilder().followRedirects(false).build()

    suspend fun getUpdated(
        service: PassWebService,
        registered: Boolean = false,
        onRegistered: suspend () -> Unit = {},
    ): UpdateResult =
        withContext(Dispatchers.IO) {
            val first = fetch(service)
            // Some issuers gate the pass behind device registration and answer 204 until the device is registered.
            if (first !is FetchOutcome.NeedsRegistration || registered || !register(service)) {
                first.toUpdateResult()
            } else {
                onRegistered()
                fetch(service).toUpdateResult()
            }
        }

    /**
     * Registers this device for the given pass using the PassKit web service protocol.
     */
    suspend fun register(service: PassWebService): Boolean {
        // There is no APNs token, issuers that validate the push token format will reject the registration.
        val body = JSONObject().put("pushToken", service.deviceId.toString()).toString()
        return succeeds(service.registrationRequest().post(body.toRequestBody(JSON_MEDIA_TYPE)).build(), 200, 201)
    }

    /**
     * Unregisters this device for the given pass using the PassKit web service protocol.
     */
    suspend fun unregister(service: PassWebService): Boolean = succeeds(service.registrationRequest().delete().build(), 200)

    private fun fetch(service: PassWebService): FetchOutcome {
        val request = service.authorizedRequest("passes/${service.passTypeIdentifier}/${service.serialNumber}").build()
        val response =
            try {
                executeFollowingRedirects(request)
            } catch (e: SocketTimeoutException) {
                Log.i(TAG, "Timeout while connecting to pass api at ${request.url}", e)
                return FetchOutcome.Result(UpdateResult.Failed(FailureReason.Timeout))
            } catch (e: IOException) {
                Log.i(TAG, "Failed to connect to pass api at ${request.url}", e)
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
                        FetchOutcome.Result(UpdateResult.Success(UpdateContent.Downloaded(body)))
                    }
                }
                304 -> FetchOutcome.Result(UpdateResult.NotUpdated)
                401, 403 -> FetchOutcome.Result(UpdateResult.Failed(FailureReason.Forbidden))
                else -> FetchOutcome.Result(UpdateResult.Failed(FailureReason.Status(it.code)))
            }
        }
    }

    private fun executeFollowingRedirects(request: Request): Response {
        var response = client.newCall(request).execute()
        repeat(MAX_REDIRECTS) {
            if (!response.isRedirect) return response
            val location = response.header("Location")?.let { response.request.url.resolve(it) } ?: return response
            response.close()
            // Issuers redirect to storage like S3 that rejects requests carrying the PassKit Authorization header.
            val redirect =
                response.request
                    .newBuilder()
                    .url(location)
                    .removeHeader("Authorization")
                    .build()
            response = client.newCall(redirect).execute()
        }
        return response
    }

    private suspend fun succeeds(
        request: Request,
        vararg successCodes: Int,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    (response.code in successCodes).also { success ->
                        if (!success) Log.i(TAG, "${request.method} ${request.url} returned ${response.code}")
                    }
                }
            } catch (e: IOException) {
                Log.i(TAG, "${request.method} ${request.url} failed", e)
                false
            }
        }

    private fun PassWebService.authorizedRequest(path: String): Request.Builder =
        Request
            .Builder()
            .url("$url/$API_VERSION/$path")
            .header("Authorization", "ApplePass $authToken")

    private fun PassWebService.registrationRequest(): Request.Builder =
        authorizedRequest("devices/$deviceId/registrations/$passTypeIdentifier/$serialNumber")

    private sealed interface FetchOutcome {
        fun toUpdateResult(): UpdateResult = (this as? Result)?.result ?: UpdateResult.NotUpdated

        data class Result(
            val result: UpdateResult,
        ) : FetchOutcome

        data object NeedsRegistration : FetchOutcome
    }

    companion object {
        private const val TAG = "PassbookApi"
        private const val API_VERSION = "v1"
        private const val MAX_REDIRECTS = 5
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
