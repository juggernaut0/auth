package auth.domain

object AuthProvider {
    const val PASSWORD = "password"
    const val GOOGLE = "google"

    private fun parseProviders(str: String): List<String> {
        if (str.isBlank()) return emptyList()
        return str
            .lowercase()
            .trim()
            .split(',')
            .map { it.trim() }
            .filter { it == PASSWORD || it == GOOGLE }
    }

    fun containsProvider(providers: String, target: String): Boolean {
        return target in parseProviders(providers)
    }

    fun appendProvider(providers: String, provider: String): String {
        val allProv = parseProviders(providers)
        return (allProv + provider).joinToString(separator = ",")
    }
}