import java.util.*
import kotlin.collections.ArrayList

interface GameDAOInterface {
    fun add(game: Game): Int
    fun update(id: Int, game: Game): Boolean
    fun delete(id: Int): Boolean
    fun get(limit: Int = 100, offset:Int = 0): ArrayList<Game>
    fun getDetailed(id: Int): Game
    fun getAll(): ArrayList<Game>
}