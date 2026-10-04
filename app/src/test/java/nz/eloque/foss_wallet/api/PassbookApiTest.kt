package nz.eloque.foss_wallet.api

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import nz.eloque.foss_wallet.model.PassWebService
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PassbookApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: PassbookApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = PassbookApi(OkHttpClient.Builder().build())
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun service(): PassWebService =
        PassWebService(
            url = server.url("/").toString().trimEnd('/'),
            authToken = "token",
            passTypeIdentifier = "pass.type.id",
            serialNumber = "serial-1",
            deviceId = UUID.randomUUID(),
        )

    @Test
    fun `304 reports not updated without registering`() {
        server.enqueue(MockResponse.Builder().code(304).build())

        val result = runBlocking { api.getUpdated(service()) }

        assertEquals(UpdateResult.NotUpdated, result)
        assertEquals(1, server.requestCount)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v1/passes/pass.type.id/serial-1", request.url.encodedPath)
        assertEquals("ApplePass token", request.headers["Authorization"])
    }

    @Test
    fun `empty 200 body reports not updated instead of crashing the parser`() {
        server.enqueue(MockResponse.Builder().code(200).build())

        val result = runBlocking { api.getUpdated(service()) }

        assertEquals(UpdateResult.NotUpdated, result)
    }

    @Test
    fun `200 body is returned as downloaded pass`() {
        server.enqueue(
            MockResponse
                .Builder()
                .code(200)
                .body("pkpass bytes")
                .build(),
        )

        val result = runBlocking { api.getUpdated(service()) }

        val content = (result as UpdateResult.Success).content as UpdateContent.Downloaded
        assertEquals("pkpass bytes", content.bytes.decodeToString())
    }

    @Test
    fun `204 triggers device registration then retries the fetch`() {
        server.enqueue(MockResponse.Builder().code(204).build())
        server.enqueue(MockResponse.Builder().code(201).build())
        server.enqueue(MockResponse.Builder().code(304).build())
        var registeredCallbacks = 0

        val result = runBlocking { api.getUpdated(service(), onRegistered = { registeredCallbacks++ }) }

        assertEquals(UpdateResult.NotUpdated, result)
        assertEquals(3, server.requestCount)
        assertEquals(1, registeredCallbacks)

        val fetch = server.takeRequest()
        assertEquals("GET", fetch.method)
        assertEquals("/v1/passes/pass.type.id/serial-1", fetch.url.encodedPath)

        val registration = server.takeRequest()
        assertEquals("POST", registration.method)
        assertTrue(
            "unexpected registration path ${registration.url.encodedPath}",
            registration.url.encodedPath
                .startsWith("/v1/devices/") &&
                registration.url.encodedPath.endsWith("/registrations/pass.type.id/serial-1"),
        )
        assertTrue(registration.body?.utf8()?.contains("pushToken") == true)

        val retry = server.takeRequest()
        assertEquals("GET", retry.method)
    }

    @Test
    fun `204 with failed registration reports not updated and does not retry`() {
        server.enqueue(MockResponse.Builder().code(204).build())
        server.enqueue(MockResponse.Builder().code(401).build())

        val result = runBlocking { api.getUpdated(service()) }

        assertEquals(UpdateResult.NotUpdated, result)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `204 for an already registered pass does not register again`() {
        server.enqueue(MockResponse.Builder().code(204).build())

        val result = runBlocking { api.getUpdated(service(), registered = true) }

        assertEquals(UpdateResult.NotUpdated, result)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `unregister deletes the registration of the pass`() {
        server.enqueue(MockResponse.Builder().code(200).build())
        val service = service()

        val unregistered = runBlocking { api.unregister(service) }

        assertTrue(unregistered)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/v1/devices/${service.deviceId}/registrations/pass.type.id/serial-1", request.url.encodedPath)
        assertEquals("ApplePass token", request.headers["Authorization"])
    }

    private fun redirect(location: String) =
        MockResponse
            .Builder()
            .code(301)
            .addHeader("Location", location)
            .build()

    @Test
    fun `redirect target is requested without the PassKit authorization`() {
        server.enqueue(redirect("/download/pass.pkpass"))
        server.enqueue(MockResponse.Builder().code(304).build())

        val result = runBlocking { api.getUpdated(service()) }

        assertEquals(UpdateResult.NotUpdated, result)
        val update = server.takeRequest()
        assertEquals("/v1/passes/pass.type.id/serial-1", update.url.encodedPath)
        assertEquals("ApplePass token", update.headers["Authorization"])
        val download = server.takeRequest()
        assertEquals("/download/pass.pkpass", download.url.encodedPath)
        assertNull(download.headers["Authorization"])
    }

    @Test
    fun `redirect loops are cut off`() {
        repeat(10) { server.enqueue(redirect("/loop")) }

        val result = runBlocking { api.getUpdated(service()) }

        assertEquals(UpdateResult.Failed(FailureReason.Status(301)), result)
        assertEquals(6, server.requestCount)
    }

    @Test
    fun `401 is reported like 403`() {
        server.enqueue(MockResponse.Builder().code(401).build())

        val result = runBlocking { api.getUpdated(service()) }

        assertEquals(UpdateResult.Failed(FailureReason.Forbidden), result)
    }
}
