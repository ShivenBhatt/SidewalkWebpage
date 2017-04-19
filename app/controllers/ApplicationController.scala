package controllers

import java.sql.Timestamp
import java.util
import java.util.{Calendar, TimeZone}
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import controllers.headers.ProvidesHeader
import models.audit.{AuditTaskTable, NewTask}
import models.user._
import models.daos.UserDAOImpl
import models.daos.slick.DBTableDefinitions.{DBUser, UserTable}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import play.api.{Logger, Play}

import scala.concurrent.Future

class ApplicationController @Inject()(implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator] with ProvidesHeader {

  val anonymousUser: DBUser = UserTable.find("anonymous").get

  /**
    * Returns an index page.
    *
    * @return
    */
  def index = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    // Get mTurk parameters
    // Map with keys ["assignmentId","hitId","turkSubmitTo","workerId"]
    val qString = request.queryString.map { case (k, v) => k.mkString -> v.mkString }
    println(qString)
    // At the end of the mission we need to create a POST request to queryString("turkSubmitTo")
    // with queryString("assignmentId") in the body
    // POST request using the scala ws API. Insert this at the end of the code for a successful mission
    // ws.url(queryString("turkSubmitTo")).post(Map("assignmentId" -> queryString("assignmentId")))
    // May require other parameters (hitId,workerId). Not sure


    var screenStatus: String = null
    if (!qString.isEmpty) {
      if (qString("assignmentId") != "ASSIGNMENT_ID_NOT_AVAILABLE") {
        // User clicked the ACCEPT HIT button
        // Redirect to the audit page
        println(qString("assignmentId"))
        screenStatus = "Assigned"
      }
      else {
        println("Preview Screen")
        screenStatus = "Preview"
      }
    }
    else {
      println("No Query String")
      screenStatus = "Blank"
    }
    WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_Index", timestamp))

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_Index", timestamp))
        Future.successful(Ok(views.html.index("Project Sidewalk", Some(user))))
      case None =>
        screenStatus match {
          case "Assigned" =>
            Future.successful(Ok(views.html.indexNext("Project Sidewalk")))
          case "Preview" =>
            Future.successful(Ok(views.html.index("Project Sidewalk")))
          case "Blank" =>
            println("No Query String")
            Future.successful(Ok(views.html.blankIndex("Project Sidewalk")))
        }
    }
  }

  /**
    * Returns an about page
    *
    * @return
    */
  def about = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_About", timestamp))
        Future.successful(Ok(views.html.about("Project Sidewalk - About", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_About", timestamp))
        Future.successful(Ok(views.html.about("Project Sidewalk - About")))
    }
  }

  def student = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_Student", timestamp))
        Future.successful(Ok(views.html.student("Project Sidewalk", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_Student", timestamp))
        Future.successful(Ok(views.html.student("Project sidewalk")))
    }

  }

  /**
    * Returns a developer page
    *
    * @return
    */
  def developer = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_Developer", timestamp))
        Future.successful(Ok(views.html.developer("Project Sidewalk - Developers", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_Developer", timestamp))
        Future.successful(Ok(views.html.developer("Project Sidewalk - Developers")))
    }
  }

  /**
    * Returns an FAQ page
    *
    * @return
    */
  def faq = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_FAQ", timestamp))
        Future.successful(Ok(views.html.faq("Project Sidewalk - About", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_FAQ", timestamp))
        Future.successful(Ok(views.html.faq("Project Sidewalk - About")))
    }
  }

  /**
    * Returns the terms page
    *
    * @return
    */
  def terms = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_Terms", timestamp))
        Future.successful(Ok(views.html.terms("Project Sidewalk - Terms", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_Terms", timestamp))
        Future.successful(Ok(views.html.terms("Project Sidewalk - Terms")))
    }
  }

  def results = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_Results", timestamp))
        Future.successful(Ok(views.html.results("Project Sidewalk - Explore Accessibility", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_Results", timestamp))
        Future.successful(Ok(views.html.results("Project Sidewalk - Explore Accessibility")))
    }
  }

  def demo = UserAwareAction.async { implicit request =>
    val now = new DateTime(DateTimeZone.UTC)
    val timestamp: Timestamp = new Timestamp(now.getMillis)
    val ipAddress: String = request.remoteAddress

    request.identity match {
      case Some(user) =>
        WebpageActivityTable.save(WebpageActivity(0, user.userId.toString, ipAddress, "Visit_Map", timestamp))
        Future.successful(Ok(views.html.accessScoreDemo("Project Sidewalk - Explore Accessibility", Some(user))))
      case None =>
        WebpageActivityTable.save(WebpageActivity(0, anonymousUser.userId.toString, ipAddress, "Visit_Map", timestamp))
        Future.successful(Ok(views.html.accessScoreDemo("Project Sidewalk - Explore Accessibility")))
    }
  }
}
