import mu.KLogging
import kotlin.collections.ArrayList

/**
 * Created by kalk on 6/20/17.
 */
class MemberDAO: MemberDAOInterface {
    companion object: KLogging()

    override fun add(member: addMember): Int {
        var id: Int
        val con = DBConnection.instance.open()
        try {
            val stmt = con.prepareStatement("insert into member (first_name, last_name) values (?,?) returning member_id")
            stmt.setString(1, member.firstName)
            stmt.setString(2,member.lastName)
            stmt.executeQuery()
            stmt.resultSet.next()
            id = stmt.resultSet.getInt(1)
        } catch (e: Exception) {
            throw APIException("could not add member $member.firstName $member.lastName")
        } finally {
            DbUtils.close(con)
        }
        return id
    }

    override fun update(id: Int, member: addMember): Boolean {
        val con = DBConnection.instance.open()
        try {
            val stmt = con.prepareStatement("update Member set first_name = ?, last_name = ? where member_id = ?")
            stmt.setString(1, member.firstName)
            stmt.setString(2, member.lastName)
            stmt.setInt(3, id)
            stmt.execute()
            MemberDAO.logger.info("Updated member $id")
            return true
        } catch (e: Exception) {
            throw APIException("could not update member $member.firstName $member.lastName")
        } finally {
            DbUtils.close(con)
        }
    }

    override fun delete(id: Int): Boolean {
        val con = DBConnection.instance.open()
        try {
            val stmt = con.prepareStatement("delete from member where member_id = ?")
            stmt.setInt(1, id)
            stmt.execute()
            MemberDAO.logger.info("Removed member $id")
            return true
        } catch (e: Exception) {
            throw APIException("Failed to delete $id")
        } finally {
            DbUtils.close(con)
        }
    }

    override fun get(limit: Int, offset: Int): ArrayList<Member> {
        val members = ArrayList<Any>()
                val con = DBConnection.instance.open()
                try {
                        val stmt = con.prepareStatement("select * from member limit ? offset ?")
                        stmt.setInt(1, limit)
                        stmt.setInt(2, offset)
                        val rs = stmt.executeQuery()
                        while (rs.next()) {
                                members.add(getMember(id = rs.getInt(1), firstName = rs.getString(2), lastName = rs.getString(3)))
                            }
                    } catch (e: Exception) {
                        throw APIException("${e.message}")
                    } finally {
                        DbUtils.close(con)
                    }
                return members
    }

    override fun getDetailed(id: Int): Member {
        val con = DBConnection.instance.open()
        val member: Member
        try {
            val stmt = con.prepareStatement("""select m.first, m.last, w.wins, l.losses, t.timesTraitor from
                                                (select count(member) as wins from winner where member = ?) as w,
                                                (select count(member) as losses from loser where member = ?) as l,
                                                (select count(member) as timesTraitor from traitor where member = ?) as t,
                                                (select first_name as first, last_name as last from member where member_id = ?) as m""")

            for(i in 1..4)
                stmt.setInt(i, id)
            val rs = stmt.executeQuery()
            rs.next()
            val wins = rs.getInt(3)
            val losses = rs.getInt(4)
            val total = wins + losses
            member = Member(id = id, firstName = rs.getString(1), lastName = rs.getString(2),
                    wins = wins, winRatio = wins.toDouble() / total, losses = losses,
                    timesTraitor = rs.getInt(5), gamesPlayed = total)
        } catch (e: Exception) {
            throw APIException("${e.message}")
        } finally {
            DbUtils.close(con)
        }
        return member
    }



}

data class addMember(val firstName: String, val lastName: String) {
    init {
        val numbers = Regex(".*\\d+.*")
        require(!firstName.matches(numbers) && !lastName.matches(numbers)) {"Invalid name."}
        require(firstName.length > 1) {"$firstName is invalid, must be at least 2 characters."}
        require(lastName.length > 1) {"$lastName is invalid, Name must be at least 2 characters."}
    }
}

data class getMember(val id: Int, val firstName: String, val lastName: String)

data class Member(val id: Int, val firstName: String, val lastName: String,
                  val wins: Int, val winRatio: Double, val losses: Int,
                  val timesTraitor: Int, val gamesPlayed: Int)

