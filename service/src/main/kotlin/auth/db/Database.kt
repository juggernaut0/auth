package auth.db

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Database @Inject constructor(private val connectionFactory: ConnectionFactory) {
    suspend fun <T> transaction(block: suspend CoroutineScope.(DSLContext) -> T): T {
        val dsl = DSL.using(connectionFactory, SQLDialect.POSTGRES)
        /*return dsl.transactionCoroutine {
            coroutineScope { block(it.dsl()) }
        }*/
        return dsl.transactionPublisher {
            mono { block(it.dsl()) }
        }.awaitFirstOrNull()
            .let {
                // Safety: If T is nullable, this is a noop, otherwise awaitFirstOrNull should never return null
                @Suppress("UNCHECKED_CAST")
                it as T
            }
    }
}
