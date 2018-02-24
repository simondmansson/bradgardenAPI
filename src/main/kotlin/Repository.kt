import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging

class Repository(val db: Database) {
    private val mapper = ObjectMapper().registerModule(KotlinModule())
    companion object: KLogging()
    val memberController = MemberController(MemberDAO(db))
    val gameController = GameController(GameDAO(db))
    val sessionController = SessionController(SessionDAO(db))
    var members = memberController.getAll()
    var games = gameController.getAll()
    var sessions = sessionController.getAll()


    fun add(member:Member): String {
        val id = memberController.add(member)
        members.add(member)
        return id
    }

    fun update(id: String, member: Member): String {
        memberController.update(id, member)
        val index = members.indexOfFirst{it.id == id.toInt()}
        members[index] = member
        return id
    }

    fun getMemberFromParams(params: HashMap<String, String>): String {
        if(params.isEmpty())
            return mapper.writeValueAsString(members)
        return ""
    }

    fun getMemberByID(id: String): String {
        return mapper.writeValueAsString(members.find{ it.id == id.toInt()})
    }

    fun removeMemberWithID(id: String) {
        memberController.removeWithID(id)
        members.removeIf { it.id == id.toInt() }
    }

    fun add(game:Game): String {
        val id = gameController.add(game)
        games.add(game)
        return id
    }

    fun update(id: String, game: Game): String {
        gameController.update(id, game)
        val index = games.indexOfFirst{it.id == id.toInt()}
        games[index] = game
        return id
    }

    fun getGameFromParams(params: HashMap<String, String>): String {
        if(params.isEmpty())
            return mapper.writeValueAsString(games)
        return ""
    }

    fun getGameByID(id: String): String {
        return mapper.writeValueAsString(games.find{it.id == id.toInt()})
    }

    fun removeGameByID(id: String) {
       gameController.removeWithID(id)
        games.removeIf{it.id == id.toInt()}
    }

    fun add(data: Session): String {
        TODO()
    }

    fun getSessionFromParams(params: HashMap<String, String>): String {
        TODO()
    }

    fun getSessionByID(id: String): String {
        TODO()
    }

    fun removeSessionWithID(id: String) {
        TODO()
    }
}