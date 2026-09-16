package org.hcs.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.hcs.tasks.Task
import org.hcs.tasks.TaskCompletionSource

data class AuthAccountResult(
    val isAuthenticated: Boolean,
    val accountEmail: String?,
    val displayName: String?,
    val grantedScopes: List<String>,
    val errorMessage: String? = null
)

class HcsAuthClient(private val context: Context) {

    fun createAuthorizationIntent(
        authEndpoint: String,
        clientId: String,
        redirectUri: String,
        scopes: List<String>
    ): Intent {
        val fullUrl = "$authEndpoint?client_id=$clientId&redirect_uri=$redirectUri&response_type=code&scope=${scopes.joinToString("+")}"
        return Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Evaluates authorization redirect URL without persisting sensitive tokens or passwords on the device.
     */
    fun handleAuthorizationUrl(redirectUrl: String?): Task<AuthAccountResult> {
        val tcs = TaskCompletionSource<AuthAccountResult>()
        if (redirectUrl.isNullOrBlank()) {
            tcs.setResult(
                AuthAccountResult(
                    isAuthenticated = false,
                    accountEmail = null,
                    displayName = null,
                    grantedScopes = emptyList(),
                    errorMessage = "No authorization result received"
                )
            )
            return tcs.task
        }

        val params = parseQueryParams(redirectUrl)
        val code = params["code"]
        val error = params["error"]

        if (code != null) {
            tcs.setResult(
                AuthAccountResult(
                    isAuthenticated = true,
                    accountEmail = params["email"] ?: "user@hcs.local",
                    displayName = "HCS Authorized User",
                    grantedScopes = listOf("openid", "profile", "email")
                )
            )
        } else {
            tcs.setResult(
                AuthAccountResult(
                    isAuthenticated = false,
                    accountEmail = null,
                    displayName = null,
                    grantedScopes = emptyList(),
                    errorMessage = error ?: "Authentication failed"
                )
            )
        }
        return tcs.task
    }

    private fun parseQueryParams(url: String): Map<String, String> {
        val queryMap = mutableMapOf<String, String>()
        val queryIndex = url.indexOf('?')
        if (queryIndex != -1 && queryIndex < url.length - 1) {
            val queryString = url.substring(queryIndex + 1)
            val pairs = queryString.split('&')
            for (pair in pairs) {
                val idx = pair.indexOf('=')
                if (idx != -1) {
                    val key = pair.substring(0, idx)
                    val value = pair.substring(idx + 1)
                    queryMap[key] = value
                }
            }
        }
        return queryMap
    }
}
