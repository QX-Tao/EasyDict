package com.qxtao.easydict.utils.common

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


object HttpUtils {
    private const val MAX_RETRY_COUNT = 3

    // 发送普通Request请求，获得对应的response.body数据
    @Throws(IOException::class)
    fun requestResult(url: String): String {
        return executeRequest(url) { response ->
            response.body!!.string()
        }
    }

    // 去除证书的Request请求，获得对应的response.body数据
    @Throws(IOException::class)
    fun requestDisableCertificateValidationResult(url: String): String {
        return executeRequest(url, disableCertificateValidation()) { response ->
            response.body!!.string()
        }
    }

    // 发送普通Request请求，返回整个response结果
    @Throws(IOException::class)
    fun requestResponse(url: String): Response {
        return executeRequest(url) { response ->
            response
        }
    }

    // 去除证书的Request请求，返回整个response结果
    @Throws(IOException::class)
    fun requestDisableCertificateValidationResponse(url: String): Response {
        return executeRequest(url, disableCertificateValidation()) { response ->
            response
        }
    }

    // 发送普通Request请求，获得对应的数据
    @Throws(IOException::class)
    fun requestBytes(url: String): ByteArray {
        return executeRequest(url) { response ->
            response.body!!.bytes()
        }
    }

    @Throws(IOException::class)
    private fun <T> executeRequest(url: String, client: OkHttpClient = OkHttpClient(), action: (Response) -> T): T {
        var retryCount = 0
        var lastException: IOException? = null
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                val request: Request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return action(response)
                    } else {
                        throw IOException("Unexpected response code: ${response.code}")
                    }
                }
            } catch (e: IOException) {
                lastException = e
                retryCount++
            }
        }
        throw lastException ?: IOException("Request failed after $MAX_RETRY_COUNT attempts")
    }

    // 判断字符串是否为URL
    fun isHttpUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: Exception) {
            false
        }
    }
}

private fun disableCertificateValidation(): OkHttpClient {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return arrayOf()
        }
    })

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
}
