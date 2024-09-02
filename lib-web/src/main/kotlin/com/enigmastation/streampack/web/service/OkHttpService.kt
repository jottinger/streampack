/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.web.service

import com.enigmastation.streampack.web.model.HttpConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Cache
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OkHttpService() : InitializingBean {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired lateinit var objectMapper: ObjectMapper

    @Autowired lateinit var configuration: HttpConfiguration

    var clients: MutableMap<Boolean, OkHttpClient> = mutableMapOf()
    lateinit var cache: Cache

    override fun afterPropertiesSet() {
        java.io.File(configuration.cacheDir).mkdirs()
        cache =
            Cache(
                directory = java.io.File(configuration.cacheDir),
                maxSize = configuration.cacheSize
            )
    }

    fun client(followRedirects: Boolean = true): OkHttpClient {
        return clients.computeIfAbsent(
            followRedirects,
            {
                OkHttpClient.Builder()
                    .cache(cache)
                    .addNetworkInterceptor { chain ->
                        chain.proceed(
                            chain
                                .request()
                                .newBuilder()
                                .header("User-Agent", configuration.userAgent)
                                .build()
                        )
                    }
                    .followRedirects(followRedirects) // Disable automatic redirect following
                    .build()
            }
        )
    }

    fun getUrl(url: String): String {
        val request = buildRequest(url)
        val response = client(true).newCall(request).execute()
        return if (response.isSuccessful) {
            response.body?.string() ?: ""
        } else {
            throw Exception("call failed: ${response.code} ${response.message}")
        }
    }

    fun buildRequest(url: String, method: String = "GET", body: RequestBody? = null): Request {
        return Request.Builder().url(url).method(method, body).build()
    }

    fun <T> get(
        url: String,
        parameters: Map<String, Any> = mapOf(),
        headers: Map<String, Any> = mapOf(),
        klass: Class<T>
    ): T {
        return execute(
            method = "GET",
            url = url,
            parameters = parameters,
            klass = klass,
            headers = headers
        )
    }

    fun <T> execute(
        method: String = "GET",
        url: String,
        parameters: Map<String, Any> = mapOf(),
        headers: Map<String, Any> = mapOf(),
        klass: Class<T>,
        body: Any? = null,
        followRedirects: Boolean = true,
    ): T {
        val urlBuilder = url.toHttpUrl().newBuilder()
        parameters.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value.toString()) }
        val headerBuilder = Headers.Builder()
        headers.forEach { (key, value) -> headerBuilder.add(key, value.toString()) }
        val request =
            Request.Builder()
                .url(urlBuilder.build())
                .method(
                    method,
                    when (method.lowercase()) {
                        "head",
                        "options",
                        "get" -> null
                        "delete",
                        "patch",
                        "put",
                        "post" ->
                            objectMapper
                                .writeValueAsString(body)
                                .toRequestBody("application/json".toMediaType())
                        else -> null
                    }
                )
                .headers(headerBuilder.build())
                .build()
        val response = client(followRedirects).newCall(request).execute()
        return if (response.isSuccessful) {
            val data = response.body?.string()
            logger.trace("response from {}: {}", request.url, data)
            objectMapper.readValue(data, klass)
        } else {
            throw Exception("Error in HTTP request: ${response.code} ${response.message}")
        }
    }
}
