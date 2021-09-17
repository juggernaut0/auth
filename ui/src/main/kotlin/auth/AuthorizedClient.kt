package auth

import multiplatform.api.ApiClient
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Headers

class AuthorizedClient(private val delegate: ApiClient): ApiClient {
    override suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
        return delegate.callApi(apiRoute, params, headers.addAuthHeader())
    }

    override suspend fun <P, T, R> callApi(
        apiRoute: ApiRouteWithBody<P, T, R>,
        params: P,
        body: T,
        headers: Headers?
    ): R {
        return delegate.callApi(apiRoute, params, body, headers.addAuthHeader())
    }

    private fun Headers?.addAuthHeader(): Headers {
        return (this?.toMutableList() ?: mutableListOf())
            .also { it.add("Authorization" to "Bearer ${getToken()}") }
            .asHeaders()
    }
}

private fun Iterable<Pair<String, String>>.asHeaders(): Headers = object : Headers, Iterable<Pair<String, String>> by this {}
