package org.hcs.proxy

import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class ProxyRequest(
    val url: String,
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap()
)

data class ProxyResponse(
    val statusCode: Int,
    val body: String,
    val isIntercepted: Boolean
)

class HcsLocalProxyServer {

    private var isRunning: Boolean = false

    fun start(): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        isRunning = true
        tcs.setResult(true)
        return tcs.task
    }

    fun stop(): Task<Boolean> {
        val tcs = TaskCompletionSource<Boolean>()
        isRunning = false
        tcs.setResult(true)
        return tcs.task
    }

    fun handleRequest(request: ProxyRequest): Task<ProxyResponse> {
        val tcs = TaskCompletionSource<ProxyResponse>()
        if (!isRunning) {
            tcs.setResult(ProxyResponse(503, "Proxy server stopped", false))
            return tcs.task
        }

        val url = request.url
        if (url.contains("googleapis.com") || url.contains("clients4.google.com")) {
            tcs.setResult(
                ProxyResponse(
                    statusCode = 200,
                    body = """{"status":"OK","hcs_intercepted":true}""",
                    isIntercepted = true
                )
            )
        } else {
            tcs.setResult(
                ProxyResponse(
                    statusCode = 200,
                    body = """{"status":"PASSTHROUGH"}""",
                    isIntercepted = false
                )
            )
        }
        return tcs.task
    }
}
