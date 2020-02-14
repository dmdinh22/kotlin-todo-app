package dmdinh

import spark.Spark.*
import kotliquery.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.HttpException

fun main(args: Array<String>) {
    port(9000)
    staticFileLocation("/public")
    setupDb()

    // test route
    get("/hello") { request, response -> "Welcome to my Kotlin Todo App" }

    // to-do routes
    path("/todo") {
        // DTO
        data class Todo(val id: Long, val text: String, val done: Boolean, val createdAt: java.time.LocalDateTime)
        val todo: (Row) -> Todo = { row -> Todo(row.long(1), row.string(2), row.boolean(3), row.localDateTime(4))}

        fun getTodo(id: Long): Todo? = using(sessionOf(HikariCP.dataSource())) { session ->
            session.run(queryOf("select id, text, done, created_at from todo where id=?", id).map(todo).asSingle)
        }

        get("/:id") { req, res ->
            jacksonObjectMapper().writeValueAsString( getTodo(req.params("id").toLong()))
        }

        get("/") { req, res ->
            val todos: List<Todo> = using(sessionOf(HikariCP.dataSource())) { session ->
                session.run( queryOf("select id, text, done, created_at from todo").map(todo).asList )
            }

            jacksonObjectMapper().writeValueAsString(todos)
        }

        post("/") { req, res ->
            try {
                if (req.body().isNullOrEmpty()) badRequest("a todo cannot be blank")

                val todo = req.body()
                val id = using(sessionOf(HikariCP.dataSource(), returnGeneratedKey = true)) { session ->
                    session.run(queryOf("insert into todo (text) values(?)", todo).asUpdateAndReturnGeneratedKey)
                }

                if (id == null) {
                    internalServerError("there was a problem creating the Todo")
                } else {
                    jacksonObjectMapper().writeValueAsString( getTodo(id) )
                }
            } catch (e: HttpException) {
                println(e)
            }
        }

        put("/:id") { req, res ->
            val update = jacksonObjectMapper().readTree(req.body())
            if (!update.has("text") || !update.has("done")) {
                badRequest("text and done is required in JSON request body")
            }

            val rowsUpdated = using(sessionOf(HikariCP.dataSource())) { session ->
                session.run(queryOf("update todo set text=?, done=? where id=?",
                        update.get("text").asText(), update.get("done").asBoolean(), req.params("id").toLong())
                        .asUpdate)
            }

            if (rowsUpdated == 1) {
                jacksonObjectMapper().writeValueAsString(getTodo(req.params("id").toLong()))
            } else {
                serverError("Something went wrong with the update, please try again later.")
            }
        }

        delete("/:id") { req, res ->
            val rowsDeleted = using(sessionOf(HikariCP.dataSource())) { session ->
                session.run(queryOf("delete from todo where id=?", req.params("id").toLong()).asUpdate)
            }

            if (rowsDeleted == 1) {
                "ok"
            } else {
                serverError("Something went wrong with the delete, please try again later.")
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
        """).asExecute) // returns bool
    }
}