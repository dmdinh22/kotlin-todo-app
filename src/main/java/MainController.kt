package dmdinh

import spark.Spark.*
import kotliquery.*
import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun main(args: Array<String>) {
    port(9000)
    staticFileLocation("/public")
    setupDb()

    path("/todo/") {
        data class Todo(val id: Long, val text: String, val done: Boolean, val createdAt: java.time.LocalDateTime)

        val todo: (Row) -> Todo = { row -> Todo(row.long(1), row.string(2), row.boolean(3), row.localDateTime(4))}

        fun getTodo(id: Long): Todo? = using(sessionOf(HikariCP.dataSource())) { session ->
            session.run(queryOf("select id, text, done, created_at from todo where id=?", id).map(todo).asSingle)
        }

        get("") { req, res -> "Hello world"}

        post("") { req, res ->
            if (req.body().isNullOrEmpty()) badRequest("a todo cannot be blank")

            val todo = req.body()
            val id = using(sessionOf(HikariCP.dataSource())) { session ->
                session.run(queryOf("insert into todo (text) values(?)", todo).asUpdateAndReturnGeneratedKey)
            }

            if (id == null) {
                internalServerError("there was a problem creating the Todo")
            } else {
                jacksonObjectMapper().writeValueAsString( getTodo(id) )
            }
        }

    }
}

// returns http status code when routes error out
fun badRequest(reason: String) = halt(400, reason)
fun serverError(reason: String) = halt(500, reason)

fun setupDb() {
    HikariCP.default("jdbc:h2:mem:todo", "user", "pass")
    // when using in-mem db, create DDL
    using(sessionOf(HikariCP.dataSource())) { session ->
        session.run(queryOf("""
          create table todo (
            id serial not null primary key,
            text varchar(255),
            done boolean default false,
            created_at timestamp not null default now()
          )
        """).asExecute) // returns Boolean
    }
}