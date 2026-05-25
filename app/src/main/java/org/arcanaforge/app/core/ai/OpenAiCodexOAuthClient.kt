package org.arcanaforge.app.core.ai

import android.net.Uri
import android.util.Base64
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class OpenAiCodexOAuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    val accountId: String,
    val email: String?,
)

class OpenAiCodexOAuthClient(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) {
    suspend fun createAuthorizationSession(): AuthorizationSession = withContext(Dispatchers.IO) {
        val verifier = randomUrlSafeToken(32)
        val challenge = codeChallenge(verifier)
        val state = randomHex(16)
        val server = LocalOAuthCallbackServer.start(expectedState = state)
        val authorizationUrl = Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", SCOPE)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)
            .appendQueryParameter("id_token_add_organizations", "true")
            .appendQueryParameter("codex_cli_simplified_flow", "true")
            .appendQueryParameter("originator", "moonlight-guidance")
            .build()
            .toString()
        AuthorizationSession(
            authorizationUrl = authorizationUrl,
            verifier = verifier,
            state = state,
            callbackServer = server,
        )
    }

    suspend fun exchangeAuthorizationCode(
        code: String,
        verifier: String,
    ): OpenAiCodexOAuthTokens = withContext(Dispatchers.IO) {
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", CLIENT_ID)
            .add("code", code)
            .add("code_verifier", verifier)
            .add("redirect_uri", REDIRECT_URI)
            .build()
        executeTokenRequest(requestBody, "exchange")
    }

    suspend fun refresh(refreshToken: String): OpenAiCodexOAuthTokens = withContext(Dispatchers.IO) {
        val requestBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", CLIENT_ID)
            .add("refresh_token", refreshToken)
            .build()
        executeTokenRequest(requestBody, "refresh")
    }

    fun parseAuthorizationInput(
        input: String,
        expectedState: String? = null,
    ): String {
        val value = input.trim()
        require(value.isNotBlank()) { "Paste the callback URL or authorization code." }
        val parsed = runCatching {
            val uri = Uri.parse(value)
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if (!code.isNullOrBlank()) {
                ParsedAuthorizationInput(code = code, state = state)
            } else {
                null
            }
        }.getOrNull()
            ?: if (value.contains("#")) {
                val parts = value.split("#", limit = 2)
                ParsedAuthorizationInput(
                    code = parts.getOrNull(0)?.takeIf { it.isNotBlank() },
                    state = parts.getOrNull(1)?.takeIf { it.isNotBlank() },
                )
            } else if (value.contains("code=")) {
                val uri = Uri.parse("https://callback.local?$value")
                ParsedAuthorizationInput(
                    code = uri.getQueryParameter("code"),
                    state = uri.getQueryParameter("state"),
                )
            } else {
                ParsedAuthorizationInput(code = value, state = null)
            }
        if (expectedState != null && parsed.state != null && parsed.state != expectedState) {
            error("OAuth state did not match.")
        }
        return parsed.code?.takeIf { it.isNotBlank() }
            ?: error("Could not find an authorization code in the pasted text.")
    }

    private fun executeTokenRequest(
        requestBody: FormBody,
        operation: String,
    ): OpenAiCodexOAuthTokens {
        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(requestBody)
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("OpenAI Codex OAuth $operation failed (${response.code}): $body")
            }
            val json = JSONObject(body)
            val accessToken = json.optString("access_token")
            val refreshToken = json.optString("refresh_token")
            val expiresIn = json.optLong("expires_in", -1L)
            if (accessToken.isBlank() || refreshToken.isBlank() || expiresIn <= 0L) {
                throw IllegalStateException("OpenAI Codex OAuth $operation response was missing token fields.")
            }
            val claims = decodeJwtClaims(accessToken)
            val authClaims = claims.optJSONObject(JWT_CLAIM_PATH)
            val accountId = authClaims?.optString("chatgpt_account_id").orEmpty()
            if (accountId.isBlank()) {
                throw IllegalStateException("OpenAI Codex OAuth token did not include an account id.")
            }
            val email = claims.optString("email").takeIf { it.isNotBlank() }
            return OpenAiCodexOAuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = Instant.now().plusSeconds(expiresIn),
                accountId = accountId,
                email = email,
            )
        }
    }

    private fun decodeJwtClaims(token: String): JSONObject {
        val parts = token.split(".")
        require(parts.size >= 2) { "OAuth access token was not a JWT." }
        val payload = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return JSONObject(payload.toString(Charsets.UTF_8))
    }

    data class AuthorizationSession(
        val authorizationUrl: String,
        val verifier: String,
        val state: String,
        private val callbackServer: LocalOAuthCallbackServer,
    ) {
        suspend fun awaitCode(): String = callbackServer.awaitCode()
        fun close() = callbackServer.close()
    }

    class LocalOAuthCallbackServer private constructor(
        private val serverSocket: ServerSocket,
        private val code: CompletableDeferred<String>,
    ) {
        suspend fun awaitCode(): String = code.await()

        fun close() {
            runCatching { serverSocket.close() }
        }

        private fun listen(expectedState: String) {
            try {
                val socket = serverSocket.accept()
                socket.use { accepted ->
                    val requestLine = accepted.getInputStream()
                        .bufferedReader()
                        .readLine()
                        .orEmpty()
                    val result = parseCallback(requestLine, expectedState)
                    if (result.isSuccess) {
                        writeHtmlResponse(
                            socket = accepted,
                            status = "200 OK",
                            body = "OpenAI authentication completed. You can close this window.",
                        )
                        code.complete(result.getOrThrow())
                    } else {
                        writeHtmlResponse(
                            socket = accepted,
                            status = "400 Bad Request",
                            body = result.exceptionOrNull()?.message ?: "OpenAI authentication failed.",
                        )
                        code.completeExceptionally(
                            result.exceptionOrNull() ?: IllegalStateException("OpenAI authentication failed."),
                        )
                    }
                }
            } catch (error: Throwable) {
                if (!code.isCompleted) {
                    code.completeExceptionally(error)
                }
            } finally {
                close()
            }
        }

        private fun parseCallback(
            requestLine: String,
            expectedState: String,
        ): Result<String> = runCatching {
            val target = requestLine.split(" ").getOrNull(1)
                ?: error("OAuth callback did not include a request target.")
            val uri = Uri.parse("http://localhost$target")
            if (uri.path != "/auth/callback") {
                error("OAuth callback route was not recognized.")
            }
            val state = uri.getQueryParameter("state")
            if (state != expectedState) {
                error("OAuth callback state did not match.")
            }
            uri.getQueryParameter("code")?.takeIf { it.isNotBlank() }
                ?: error("OAuth callback did not include an authorization code.")
        }

        private fun writeHtmlResponse(socket: Socket, status: String, body: String) {
            val html = """
                <!doctype html>
                <html><head><meta charset="utf-8"><title>Moonlight Guidance</title></head>
                <body><h1>Moonlight Guidance</h1><p>${body.escapeHtml()}</p></body></html>
            """.trimIndent()
            BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8)).use { writer ->
                writer.write("HTTP/1.1 $status\r\n")
                writer.write("Content-Type: text/html; charset=utf-8\r\n")
                writer.write("Content-Length: ${html.toByteArray(Charsets.UTF_8).size}\r\n")
                writer.write("Connection: close\r\n")
                writer.write("\r\n")
                writer.write(html)
                writer.flush()
            }
        }

        companion object {
            suspend fun start(expectedState: String): LocalOAuthCallbackServer =
                withContext(Dispatchers.IO) {
                    val socket = ServerSocket(PORT, 1, InetAddress.getByName(HOST))
                    val server = LocalOAuthCallbackServer(
                        serverSocket = socket,
                        code = CompletableDeferred(),
                    )
                    Thread {
                        server.listen(expectedState)
                    }.apply {
                        name = "OpenAiCodexOAuthCallback"
                        isDaemon = true
                        start()
                    }
                    server
                }
        }
    }

    private companion object {
        const val HOST = "127.0.0.1"
        const val PORT = 1455
        const val CLIENT_ID = "app_EMoamEEZ73f0CkXaXp7hrann"
        const val AUTHORIZE_URL = "https://auth.openai.com/oauth/authorize"
        const val TOKEN_URL = "https://auth.openai.com/oauth/token"
        const val REDIRECT_URI = "http://localhost:1455/auth/callback"
        const val SCOPE = "openid profile email offline_access"
        const val JWT_CLAIM_PATH = "https://api.openai.com/auth"

        fun randomUrlSafeToken(byteCount: Int): String {
            val bytes = ByteArray(byteCount)
            SecureRandom().nextBytes(bytes)
            return Base64.encodeToString(
                bytes,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
            )
        }

        fun randomHex(byteCount: Int): String {
            val bytes = ByteArray(byteCount)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString(separator = "") { "%02x".format(it) }
        }

        fun codeChallenge(verifier: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(verifier.toByteArray(Charsets.US_ASCII))
            return Base64.encodeToString(
                digest,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
            )
        }

        fun String.escapeHtml(): String =
            replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
    }

    private data class ParsedAuthorizationInput(
        val code: String?,
        val state: String?,
    )
}
