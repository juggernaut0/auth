package auth

import asynclite.async
import auth.api.v1.PasswordRegistrationRequest
import auth.api.v1.PasswordSignInRequest
import auth.api.v1.authModule
import kotlinx.serialization.json.Json
import kui.*
import multiplatform.FetchException
import multiplatform.call
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLStyleElement
import kotlinx.browser.document
import kotlinx.browser.window

class AuthPanel : Component() {
    private val json = Json { ignoreUnknownKeys = false; serializersModule = authModule }

    private var registrationMode: Boolean by renderOnSet(false)

    private var email = ""
    private var password = ""
    private var confirmPassword = ""
    private var displayName = ""

    private var errorText: String? by renderOnSet(null)

    private var submitting by renderOnSet(false)

    private fun register() {
        if (email.isEmpty() || password.isEmpty()) {
            errorText = "Required fields are missing."
            return
        }

        if (password != confirmPassword) {
            errorText = "Password confirmation does not match."
            return
        }

        submitting = true

        async {
            try {
                val req = PasswordRegistrationRequest(email, displayName.takeIf { it.isNotBlank() }, password)
                val user = auth.api.v1.register.call(req, Unit, json = json)
                setToken(user.token)
                (document.getElementById("reg-form") as HTMLFormElement).submit() // reloads the page on chrome
                window.location.reload()
            } catch (e: FetchException) {
                errorText = "Registration failed: ${e.message}"
                submitting = false
            }
        }
    }

    private fun signIn() {
        if (email.isEmpty() || password.isEmpty()) {
            errorText = "Required fields are missing."
            return
        }

        submitting = true

        async {
            try {
                val user = auth.api.v1.signIn.call(PasswordSignInRequest(email, password), Unit, json = json)
                setToken(user.token)
                (document.getElementById("signin-form") as HTMLFormElement).submit() // reloads the page on chrome
                window.location.reload()
            } catch (e: FetchException) {
                errorText = "Sign in failed. Check that your email and password are correct."
                submitting = false
            }
        }
    }

    override fun render() {
        markup().div(classes("signin-container")) {
            div(classes("signin-header-box")) {
                h4(classes("signin-header")) {
                    if (registrationMode) {
                        +"Register"
                    } else {
                        +"Sign In"
                    }
                }
            }
            div(classes("signin-form")) {
                if (registrationMode) {
                    registrationForm()
                } else {
                    signInForm()
                }
            }
        }
    }

    private fun MarkupBuilder.signInForm() {
        form(Props(id = "signin-form")) {
            label(classes("signin-label")) {
                +"Email"
                inputText(classes("signin-input"), autocomplete = "email", model = ::email)
            }
            label(classes("signin-label")) {
                +"Password"
                inputPassword(classes("signin-input"), autocomplete = "current-password", model = ::password)
            }
        }
        if (errorText != null) {
            span(classes("signin-errortext")) { +errorText!! }
        }
        button(Props(
                classes = listOf("signin-button"),
                disabled = submitting,
                click = { signIn() }
        )) {
            +"Sign In"
        }
        button(Props(
                classes = listOf("signin-button-outline"),
                disabled = submitting,
                click = {
                    password = ""
                    confirmPassword = ""
                    registrationMode = !registrationMode
                    errorText = null
                }
        )) {
            +"Create an account"
        }
    }

    private fun MarkupBuilder.registrationForm() {
        form(Props(id = "reg-form")) {
            label(classes("signin-label")) {
                +"Email"
                inputText(classes("signin-input"), autocomplete = "email", model = ::email)
            }
            label(classes("signin-label")) {
                +"Password"
                inputPassword(classes("signin-input"), autocomplete = "new-password", model = ::password)
            }
            label(classes("signin-label")) {
                +"Confirm password"
                inputPassword(classes("signin-input"), autocomplete = "new-password", model = ::confirmPassword)
            }
            label(classes("signin-label")) {
                +"Display Name"
                small(classes("signin-muted")) { +" (Optional)" }
                inputText(classes("signin-input"), model = ::displayName)
            }
        }
        if (errorText != null) {
            span(classes("signin-errortext")) { +errorText!! }
        }
        button(Props(
                classes = listOf("signin-button"),
                disabled = submitting,
                click = { register() }
        )) {
            +"Create Account"
        }
        button(Props(
                classes = listOf("signin-button-outline"),
                disabled = submitting,
                click = {
                    password = ""
                    confirmPassword = ""
                    registrationMode = !registrationMode
                    errorText = null
                }
        )) {
            +"Back"
        }
    }

    object Styles {
        fun apply() {
            appendCss("""
                .signin-container {
                    width: 100%;
                    border: 1px solid #ccc;
                    border-radius: 0.5rem;
                    background-color: #f7f7f7;
                    font-family: "Segoe UI",Roboto,sans-serif;
                    color: #222;
                }
                
                @media only screen and (min-width: 600px) {
                    .signin-container {
                        width: 500px;
                        margin: 0 auto;
                    }
                }
                
                .signin-header-box {
                    width: 100%;
                    padding: 0.6rem 0;
                    border-bottom: 1px solid #ccc;
                }
                
                .signin-header {
                    font-size: 1.25rem;
                    font-weight: 500;
                    margin-block-start: 0.4rem;
                    margin-block-end: 0.4rem;
                    margin-inline-start: 1rem;
                }
                
                .signin-form {
                    padding: 1rem 0.75rem;
                }
                
                .signin-label {
                    display: block;
                    width: 100%;
                    margin-bottom: 1.5rem;
                }
                
                .signin-input {
                    width: 100%;
                    display: inline-block;
                    box-sizing: border-box;
                    padding: 0.375rem 0.5rem;
                    border: 1px solid #ccc;
                    border-radius: 0.2rem;
                    line-height: 1.5;
                    font-size: 1rem;
                    color: #333;
                    margin-top: 0.4rem;
                }
                
                .signin-button {
                    display: block;
                    width: 100%;
                    height: 3rem;
                    background-color: #2a4;
                    color: #fff;
                    border: 0;
                    border-radius: 0.25rem;
                    margin-top: 0.5rem;
                    font-size: 1rem;
                }
                
                .signin-button:hover {
                    background-color: #183;
                }
                
                .signin-button-outline {
                    display: block;
                    width: 100%;
                    height: 3rem;
                    background-color: #fff;
                    color: #2a4;
                    border: 2px solid #2a4;
                    border-radius: 0.25rem;
                    margin-top: 0.5rem;
                    font-size: 1rem;
                }
                
                .signin-button-outline:hover {
                    border-color: #183;
                    color: #183;
                }
                
                .signin-muted {
                    color: #999;
                }
                
                .signin-errortext {
                    color: #c20;
                }
            """.trimIndent())
        }
    }
}

private fun appendCss(css: String) {
    val styleElem = document.createElement("style") as HTMLStyleElement
    styleElem.type = "text/css"
    styleElem.innerHTML = css
    document.head?.appendChild(styleElem)
}


