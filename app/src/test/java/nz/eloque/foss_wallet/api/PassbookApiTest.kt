package nz.eloque.foss_wallet.api

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

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

    private fun pass(): Pass =
        Pass(
            id = "pass-id",
            description = "desc",
            formatVersion = 1,
            organization = "org",
            serialNumber = "serial-1",
            type = PassType.Generic,
            barCodes = setOf(),
            addedAt = Instant.ofEpochMilli(0),
            authToken = "token",
            webServiceUrl = server.url("/").toString().trimEnd('/'),
            passTypeIdentifier = "pass.type.id",
        )

    @Test
    fun `304 reports not updated without registering`() {
        server.enqueue(MockResponse.Builder().code(304).build())

        val result = runBlocking { api.getUpdated(pass()) }

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

        val result = runBlocking { api.getUpdated(pass()) }

        assertEquals(UpdateResult.NotUpdated, result)
    }

    @Test
    fun `garbage 200 body fails gracefully`() {
        server.enqueue(
            MockResponse
                .Builder()
                .code(200)
                .body("not a pkpass")
                .build(),
        )

        val result = runBlocking { api.getUpdated(pass()) }

        assertTrue(result is UpdateResult.Failed)
    }

    @Test
    fun `204 triggers device registration then retries the fetch`() {
        server.enqueue(MockResponse.Builder().code(204).build())
        server.enqueue(MockResponse.Builder().code(201).build())
        server.enqueue(MockResponse.Builder().code(304).build())

        val result = runBlocking { api.getUpdated(pass()) }

        assertEquals(UpdateResult.NotUpdated, result)
        assertEquals(3, server.requestCount)

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

        val result = runBlocking { api.getUpdated(pass()) }

        assertEquals(UpdateResult.NotUpdated, result)
        assertEquals(2, server.requestCount)
    }
}
