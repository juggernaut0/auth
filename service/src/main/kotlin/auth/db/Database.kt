package auth.db

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.kotlin.coroutines.transactionCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@Singleton
class Database @Inject constructor(private val connectionFactory: ConnectionFactory) {
    suspend fun <T> transaction(block: suspend CoroutineScope.() -> T): T {
        val dsl = DSL.using(connectionFactory, SQLDialect.POSTGRES)
        /*return dsl.transactionCoroutine {
            withContext(DSLContextContext(it.dsl())) { block() }
        }*/
        return dsl.transactionPublisher {
            val ctx = it.dsl()
            mono(DSLContextContext(ctx)) { block() }
        }.awaitFirstOrNull()
            .let {
                // Safety: If T is nullable, this is a noop, otherwise awaitFirstOrNull should never return null
                @Suppress("UNCHECKED_CAST")
                it as T
            }
    }
}

class DSLContextContext(val ctx: DSLContext) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<DSLContextContext>
}

suspend fun dslContext() = coroutineContext[DSLContextContext]!!.ctx
