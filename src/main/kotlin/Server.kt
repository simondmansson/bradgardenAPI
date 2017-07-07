import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import spark.Spark.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
/**
 * Created by kalk on 7/5/17.
 */
const val ENDPOINTS = "/api/endpoints"
const val MEMBERS = "/api/members"
const val MEMBERSID = "/api/members/:id"
const val GAMES = "/api/games"
const val GAMESID = "/api/game/:id"
const val SESSIONS = "/api/sessions"
const val SESSIONSID ="/api/sessions/:id"
const val JSON ="application/json"

class APIException(message: String) : Exception(message)

class Server {
    companion object: KLogging()
    fun start() {
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        val publicEndpoints = hashMapOf("members" to MEMBERS, "games" to GAMES, "sessions" to SESSIONS)
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val auth = Authorization()
        port(8080)

        get(ENDPOINTS) { req, res ->
            res.type(JSON)
            mapper.writeValueAsString(publicEndpoints)
        }

        post(MEMBERS) { req, res ->
            try {
                val member = mapper.readValue<addMember>(req.body())
                var id = MemberDAO().add(member.firstName, member.lastName)
                res.type(JSON)
                res.status(201)
                toJSON("id", id)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        get(MEMBERS) { req, res ->
            try {
                val pageStart: Int? = req.queryParams("pageStart")?.toInt()
                val pageSize: Int? = req.queryParams("pageSize")?.toInt()
                res.type(JSON)
                if (pageStart != null && pageSize != null) {
                    mapper.writeValueAsString(MemberDAO().get(limit = pageSize, offset = pageStart))
                } else if (pageStart == null && pageSize != null) {
                    mapper.writeValueAsString(MemberDAO().get(limit = pageSize))
                } else if (pageStart != null && pageSize == null) {
                    mapper.writeValueAsString(MemberDAO().get(offset = pageStart))
                } else {
                    mapper.writeValueAsString(MemberDAO().get())
                }
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        get(MEMBERSID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                res.type(JSON)
                mapper.writeValueAsString(MemberDAO().getDetailed(id))
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        put(MEMBERSID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                val member = mapper.readValue<addMember>(req.body())
                if (MemberDAO().update(member.firstName, member.lastName, id = id)) {
                    res.status(204)
                } else {
                    res.status(400)
                }
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        delete(MEMBERSID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                res.status(204)
                MemberDAO().delete(id)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        post(GAMES) { req, res ->
            try {
                val game = mapper.readValue<AddGame>(req.body())
                var id = GameDAO().add(game.name, game.maxNumOfPlayers, game.traitor, game.coop)
                res.type(JSON)
                res.status(201)
                toJSON("id", id)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        get(GAMES) { req, res ->
            try {
                val pageStart: Int? = req.queryParams("pageStart")?.toInt()
                val pageSize: Int? = req.queryParams("pageSize")?.toInt()
                res.type(JSON)
                if (pageStart != null && pageSize != null) {
                    mapper.writeValueAsString(GameDAO().get(limit = pageSize, offset = pageStart))
                } else if (pageStart == null && pageSize != null) {
                    mapper.writeValueAsString(GameDAO().get(limit = pageSize))
                } else if (pageStart != null && pageSize == null) {
                    mapper.writeValueAsString(GameDAO().get(offset = pageStart))
                } else {
                    mapper.writeValueAsString(GameDAO().get())
                }
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        put(GAMESID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                val game = mapper.readValue<AddGame>(req.body())
                GameDAO().update(game.name, game.maxNumOfPlayers, game.traitor, game.coop, id)
                res.type(JSON)
                res.status(204)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        delete(GAMESID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                res.status(204)
                MemberDAO().delete(id)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        post(SESSIONS) { req, res ->
            try {
                val session = mapper.readValue<addSession>(req.body())
                res.type(JSON)
                res.status(201)
                val id = SessionDAO().add(gameID = session.gameID, date = dtf.format(LocalDateTime.now()), winners = session.winners,
                        losers = session.losers, traitors = session.traitors)
                toJSON("id", id)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

        get(SESSIONS) { req, res ->
            try {
                val pageStart: Int? = req.queryParams("pageStart")?.toInt()
                val pageSize: Int? = req.queryParams("pageSize")?.toInt()
                res.type(JSON)
                if (pageStart != null && pageSize != null) {
                    mapper.writeValueAsString(SessionDAO().get(limit = pageSize, offset = pageStart))
                } else if (pageStart == null && pageSize != null) {
                    mapper.writeValueAsString(SessionDAO().get(limit = pageSize))
                } else if (pageStart != null && pageSize == null) {
                    mapper.writeValueAsString(SessionDAO().get(offset = pageStart))
                } else {
                    mapper.writeValueAsString(SessionDAO().get())
                }
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

       get(SESSIONSID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                res.type(JSON)
                mapper.writeValueAsString(SessionDAO().getDetailed(id))
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

       delete(SESSIONSID) { req, res ->
            try {
                val id = req.params(":id").toInt()
                res.status(204)
                SessionDAO().delete(id)
            } catch (e: Exception) {
                throw APIException("Error: ${e.message}")
            }
        }

       exception(APIException::class.java, { exception, req, res ->
            res.status(400)
            res.type(JSON)
            res.body(exception.message)
        })

       after("/*") { req, res ->
           logger.info("${req.ip()}")
       }
    }

    fun toJSON(key: Any, value: Any): String {
        return "{\"${key}\": \"${value}\"}"
    }

    fun log(ip:String, method: String, user: String) {
        logger.info("""$method request by $user from $ip""")
    }
}