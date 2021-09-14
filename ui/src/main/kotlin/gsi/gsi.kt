package gsi

import org.w3c.dom.Element

external val google: Google

external class Google {
    val accounts: Accounts
}

external class Accounts {
    val id: Id
}

external class Id {
    fun initialize(options: dynamic)
    fun renderButton(element: Element, options: dynamic)
}

external class CredentialResponse {
    val credential: String
}
