import auth.db.jooq.Tables
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.jooq.DataType
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.kotlin.coroutines.transactionCoroutine
import reactor.core.publisher.Mono
import kotlin.test.Test

class DatabaseTest {
    @Test
    fun test() {
        val options = ConnectionFactoryOptions.parse("r2dbc:postgresql://localhost:6432/auth")
            .mutate()
            .option(ConnectionFactoryOptions.USER, "auth")
            .option(ConnectionFactoryOptions.PASSWORD, "auth")
            .build()
        val fac = ConnectionFactories.get(options)

        val dsl = DSL.using(fac)
        val fooTable = DSL.table("foo")
        val numField = DSL.field("num", SQLDataType.INTEGER)
        dsl.dropTableIfExists(fooTable)
            .let { Mono.from(it) }
            .block()
            .also { println(it.toString()) }
        dsl.createTable(fooTable)
            .column(numField)
            .let { Mono.from(it) }
            .block()
            .also { println(it.toString()) }

        dsl.transactionPublisher {
            val innerCtx = it.dsl()
            Mono.from(innerCtx.insertInto(fooTable).set(numField, 1)).flatMap {
                Mono.from(innerCtx.selectCount().from(fooTable))
            }
        }.let { Mono.from(it) }.block().also { println(it) }

        dsl.transactionPublisher {
            Mono.from(it.dsl().selectCount().from(fooTable))
        }.let { Mono.from(it) }.block().also { println(it) }
    }
}