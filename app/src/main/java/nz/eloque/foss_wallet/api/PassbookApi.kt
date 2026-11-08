package nz.eloque.foss_wallet.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import kotlinx.io.IOException
import nz.eloque.foss_wallet.model.PassWebService
import org.json.JSONObject

class PassbookApi(
    client: HttpClient = HttpClient(OkHttp),
) {
    private val client =
        client.config {
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = TIMEOUT_MILLIS
                socketTimeoutMillis = TIMEOUT_MILLIS
            }
        }

    suspend fun getUpdated(
        service: PassWebService,
        registered: Boolean = false,
        onRegistered: suspend () -> Unit = {},
    ): UpdateResult {
        val first = fetch(service)
        // Some issuers gate the pass behind device registration and answer 204 until the device is registered.
        return if (first !is FetchOutcome.NeedsRegistration || registered || !register(service)) {
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
        return succeeds(HttpMethod.Post, service, body, 200, 201)
    }

    /**
     * Unregisters this device for the given pass using the PassKit web service protocol.
     */
    suspend fun unregister(service: PassWebService): Boolean = succeeds(HttpMethod.Delete, service, null, 200)

    private suspend fun fetch(service: PassWebService): FetchOutcome {
        val url = service.apiUrl("passes/${service.passTypeIdentifier}/${service.serialNumber}")
        val response =
            try {
                getFollowingRedirects(url, service.authToken)
            } catch (e: IOException) {
                if (e is HttpRequestTimeoutException || e is ConnectTimeoutException || e is SocketTimeoutException) {
                    Log.i(TAG, "Timeout while connecting to pass api at $url", e)
                    return FetchOutcome.Result(UpdateResult.Failed(FailureReason.Timeout))
                }
                Log.i(TAG, "Failed to connect to pass api at $url", e)
                return FetchOutcome.Result(UpdateResult.Failed(FailureReason.Exception(e)))
            }
        return when (val status = response.status.value) {
            204 -> {
                FetchOutcome.NeedsRegistration
            }

            200 -> {
                val body = response.readRawBytes()
                if (body.isEmpty()) {
                    FetchOutcome.Result(UpdateResult.NotUpdated)
                } else {
                    FetchOutcome.Result(UpdateResult.Success(UpdateContent.Downloaded(body)))
                }
            }

            304 -> {
                FetchOutcome.Result(UpdateResult.NotUpdated)
            }

            401, 403 -> {
                FetchOutcome.Result(UpdateResult.Failed(FailureReason.Forbidden))
            }

            else -> {
                FetchOutcome.Result(UpdateResult.Failed(FailureReason.Status(status)))
            }
        }
    }

    private suspend fun getFollowingRedirects(
        url: String,
        authToken: String,
    ): HttpResponse {
        var response = client.get(url) { header(HttpHeaders.Authorization, "ApplePass $authToken") }
        repeat(MAX_REDIRECTS) {
            val location = response.headers[HttpHeaders.Location]
            if (response.status.value !in REDIRECT_CODES || location == null) return response
            // Issuers redirect to storage like S3 that rejects requests carrying the PassKit Authorization header.
            response = client.get(URLBuilder(response.request.url).takeFrom(location).build())
        }
        return response
    }

    private suspend fun succeeds(
        method: HttpMethod,
        service: PassWebService,
        body: String?,
        vararg successCodes: Int,
    ): Boolean {
        val url = service.apiUrl("devices/${service.deviceId}/registrations/${service.passTypeIdentifier}/${service.serialNumber}")
        return try {
            val response =
                client.request(url) {
                    this.method = method
                    header(HttpHeaders.Authorization, "ApplePass ${service.authToken}")
                    if (body != null) {
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    }
                }
            (response.status.value in successCodes).also { success ->
                if (!success) Log.i(TAG, "${method.value} $url returned ${response.status.value}")
            }
        } catch (e: IOException) {
            Log.i(TAG, "${method.value} $url failed", e)
            false
        }
    }

    private fun PassWebService.apiUrl(path: String): String = "$url/$API_VERSION/$path"

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
        private const val TIMEOUT_MILLIS = 10_000L
        private val REDIRECT_CODES = setOf(300, 301, 302, 303, 307, 308)
    }
}
