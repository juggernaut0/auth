package auth

import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.browser.window

private const val TOKEN_KEY = "auth-token"

fun getToken(): String? {
    return window.localStorage[TOKEN_KEY]
}

fun setToken(token: String) {
    window.localStorage[TOKEN_KEY] = token
}

fun isSignedIn(): Boolean = getToken() != null

fun signOut() {
    window.localStorage.removeItem(TOKEN_KEY)
}
