package com.grensil.network

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HttpClientTest {

    private lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        httpClient = HttpClient()
    }

    @Test
    fun `HttpResponse data class works correctly`() {
        // Given
        val headers = mapOf("Accept" to "application/json")
        
        // When
        val response = HttpResponse(
            statusCode = 200,
            headers = headers,
            body = "test response"
        )

        // Then
        assertEquals(200, response.statusCode)
        assertEquals(headers, response.headers)
        assertEquals("test response", response.body)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun `HttpResponse isSuccessful returns true for 2xx codes`() {
        assertTrue(HttpResponse(200, emptyMap(), "OK").isSuccessful)
        assertTrue(HttpResponse(201, emptyMap(), "Created").isSuccessful)
        assertTrue(HttpResponse(204, emptyMap(), "No Content").isSuccessful)
        assertTrue(HttpResponse(299, emptyMap(), "Custom Success").isSuccessful)
    }

    @Test
    fun `HttpResponse isSuccessful returns false for non-2xx codes`() {
        assertFalse(HttpResponse(199, emptyMap(), "Custom").isSuccessful)
        assertFalse(HttpResponse(300, emptyMap(), "Multiple Choices").isSuccessful)
        assertFalse(HttpResponse(400, emptyMap(), "Bad Request").isSuccessful)
        assertFalse(HttpResponse(404, emptyMap(), "Not Found").isSuccessful)
        assertFalse(HttpResponse(500, emptyMap(), "Internal Server Error").isSuccessful)
    }

    @Test
    fun `HttpResponse utility methods work correctly`() {
        // Test getContentType
        val jsonResponse = HttpResponse(200, mapOf("Content-Type" to "application/json"), "")
        assertEquals("application/json", jsonResponse.getContentType())

        // Test getContentLength
        val lengthResponse = HttpResponse(200, mapOf("Content-Length" to "123"), "")
        assertEquals(123L, lengthResponse.getContentLength())

        // Test isJson
        assertTrue(jsonResponse.isJson())
        assertFalse(HttpResponse(200, emptyMap(), "").isJson())

        // Test isClientError
        assertTrue(HttpResponse(404, emptyMap(), "").isClientError())
        assertFalse(HttpResponse(200, emptyMap(), "").isClientError())

        // Test isServerError
        assertTrue(HttpResponse(500, emptyMap(), "").isServerError())
        assertFalse(HttpResponse(200, emptyMap(), "").isServerError())

        // Test isRedirect
        assertTrue(HttpResponse(301, emptyMap(), "").isRedirect())
        assertFalse(HttpResponse(200, emptyMap(), "").isRedirect())
    }

    @Test
    fun `NhnNetworkException hierarchy works correctly`() {
        val cause = RuntimeException("Original error")
        
        val connectionException = NhnNetworkException.ConnectionExceptionNhn("Connection failed", cause)
        assertTrue(connectionException is NhnNetworkException)
        assertEquals("Connection failed", connectionException.message)
        assertEquals(cause, connectionException.cause)

        val timeoutException = NhnNetworkException.TimeoutExceptionNhn("Timeout occurred", cause)
        assertTrue(timeoutException is NhnNetworkException)
        assertEquals("Timeout occurred", timeoutException.message)
        assertEquals(cause, timeoutException.cause)

        val sslException = NhnNetworkException.SSLExceptionNhn("SSL error", cause)
        assertTrue(sslException is NhnNetworkException)
        assertEquals("SSL error", sslException.message)
        assertEquals(cause, sslException.cause)

        val invalidUrlException = NhnNetworkException.InvalidUrlExceptionNhn("Invalid URL", cause)
        assertTrue(invalidUrlException is NhnNetworkException)
        assertEquals("Invalid URL", invalidUrlException.message)
        assertEquals(cause, invalidUrlException.cause)

        val httpException = NhnNetworkException.HttpExceptionNhn(404, "HTTP error", "response")
        assertTrue(httpException is NhnNetworkException)
        assertEquals("HTTP error", httpException.message)
        assertEquals(404, httpException.statusCode)
        assertEquals("response", httpException.response)
    }

    @Test
    fun `HttpRequest data class works correctly`() {
        // Given
        val headers = mapOf("Authorization" to "Bearer token")
        
        // When
        val request = HttpRequest(
            url = "https://api.example.com",
            method = HttpMethod.POST,
            headers = headers,
            body = "test body",
            timeoutMs = 5000
        )

        // Then
        assertEquals("https://api.example.com", request.url)
        assertEquals(HttpMethod.POST, request.method)
        assertEquals(headers, request.headers)
        assertEquals("test body", request.body)
        assertEquals(5000, request.timeoutMs)
    }

    @Test
    fun `HttpMethod enum has correct values`() {
        assertEquals("GET", HttpMethod.GET.name)
        assertEquals("POST", HttpMethod.POST.name)
        assertEquals("PUT", HttpMethod.PUT.name)
        assertEquals("DELETE", HttpMethod.DELETE.name)
        
        assertEquals("GET", HttpMethod.GET.value)
        assertEquals("POST", HttpMethod.POST.value)
        assertEquals("PUT", HttpMethod.PUT.value)
        assertEquals("DELETE", HttpMethod.DELETE.value)
    }

    @Test
    fun `HttpResponse extension functions work correctly`() {
        // Test asString
        val response = HttpResponse(200, emptyMap(), "test content")
        assertEquals("test content", response.asString())

        // Test asBytes
        val bytes = response.asBytes()
        assertArrayEquals("test content".toByteArray(Charsets.UTF_8), bytes)

        // Test asInt
        val intResponse = HttpResponse(200, emptyMap(), "123")
        assertEquals(123, intResponse.asInt())

        val invalidIntResponse = HttpResponse(200, emptyMap(), "not a number")
        assertNull(invalidIntResponse.asInt())

        // Test asLong
        val longResponse = HttpResponse(200, emptyMap(), "123456789")
        assertEquals(123456789L, longResponse.asLong())

        // Test asBoolean
        val boolResponse = HttpResponse(200, emptyMap(), "true")
        assertEquals(true, boolResponse.asBoolean())

        val falseBoolResponse = HttpResponse(200, emptyMap(), "false")
        assertEquals(false, falseBoolResponse.asBoolean())
    }

    @Test
    fun `HttpClient instance can be created`() {
        // Just verify that HttpClient can be instantiated without errors
        val client = HttpClient()
        assertNotNull(client)
    }

    // Note: We cannot easily test the actual HTTP methods (get, post, etc.) without
    // a real server or complex mocking of HttpURLConnection. The integration tests
    // handle testing the actual HTTP functionality.
}