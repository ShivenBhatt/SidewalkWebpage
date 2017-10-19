package models.user

import models.utils.MyPostgresDriver.simple._
import play.api.Play.current
import java.util.UUID

import scala.util.control.NonFatal

case class UserRole(userRoleId: Int, userId: String, roleId: Int)

class UserRoleTable(tag: Tag) extends Table[UserRole](tag, Some("sidewalk"), "user_role") {
  def userRoleId = column[Int]("user_role_id", O.PrimaryKey, O.AutoInc)
  def userId = column[String]("user_id", O.NotNull)
  def roleId = column[Int]("role_id", O.NotNull)


  def * = (userRoleId, userId, roleId) <> ((UserRole.apply _).tupled, UserRole.unapply)
}

object UserRoleTable {
  val db = play.api.db.slick.DB
  val userRoles = TableQuery[UserRoleTable]
  val roles = TableQuery[RoleTable]

  val roleMapping = Map("User" -> 1, "Turker" -> 2, "Researcher" -> 3, "Administrator" -> 4, "Owner" -> 5)


  def getRole(userId: UUID): String = db.withSession { implicit session =>
    val _roles = for {
      (_userRoles, _roles) <- userRoles.innerJoin(roles).on(_.roleId === _.roleId) if _userRoles.userId === userId.toString
    } yield _roles
    try {
      _roles.list.map(_.role).head
    } catch {
      // no role found, give them User role
      case NonFatal(t) =>
        setRole(userId, "User")
        "User"
    }
  }

  def setRole(userId: UUID, newRole: String): Int = db.withTransaction { implicit session =>
    setRole(userId, roleMapping(newRole))
  }

  def setRole(userId: UUID, newRole: Int): Int = db.withTransaction { implicit session =>
    val userRoleId: Option[Int] = userRoles.filter(_.userId === userId.toString).map(_.userRoleId).list.headOption
    userRoles.insertOrUpdate(UserRole(userRoleId.getOrElse(0), userId.toString, newRole))
//    val q = for{ l <- userRoles if l.userId === userId.toString } yield l.roleId
//    q.update(newRole)
  }

  def isResearcher(userId: UUID): Boolean = db.withSession { implicit session =>
    getRole(userId) == "Researcher"
  }
}