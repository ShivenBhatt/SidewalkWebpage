package controllers

import java.sql.Timestamp
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import controllers.headers.ProvidesHeader
import models.clustering_session._
import models.daos.slick.DBTableDefinitions.{DBUser, UserTable}
import models.user.User
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{JsError, JsObject, Json}
import play.api.mvc.BodyParsers

import scala.concurrent.Future
import scala.sys.process._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Lab(labelId: Int, labelType: String, clusterNum: Int)
case class Bleep(labelId: Int, labelType: String)

class ClusteringSessionController @Inject()(implicit val env: Environment[User, SessionAuthenticator])
  extends Silhouette[User, SessionAuthenticator] with ProvidesHeader {

  implicit val labReads: Reads[Lab] = (
    (JsPath \ "label_id").read[Int] and
      (JsPath \ "label_type").read[String] and
        (JsPath \ "cluster").read[Int]
    )(Lab.apply _)

  // Helper methods
  def isAdmin(user: Option[User]): Boolean = user match {
    case Some(user) =>
      if (user.roles.getOrElse(Seq()).contains("Administrator")) true else false
    case _ => false
  }

  // Pages
  def index = UserAwareAction.async { implicit request =>
//    if (isAdmin(request.identity)) {
      Future.successful(Ok(views.html.clustering("Project Sidewalk", request.identity)))
//    } else {
//      Future.successful(Redirect("/"))
//    }
  }

  def runClustering(routeId: Int) = UserAwareAction.async { implicit request =>
//    if (isAdmin(request.identity)) {
      val clusteringOutput = ("python label_clustering.py " + routeId).!!
//      println(clusteringOutput)
      val testJson = Json.obj("what did we run?" -> "clustering!", "output" -> "something")
      Future.successful(Ok(testJson))
//    } else {
//      Future.successful(Redirect("/"))
//    }
  }

  /**
    * Returns all records in clustering_session table that are not marked as deleted.
    */
  def getClusteringSessionsWithoutDeleted = UserAwareAction.async { implicit request =>
    val clusteringSessions= ClusteringSessionTable.selectSessionsWithoutDeleted

    val ses: List[JsObject] = clusteringSessions.map { clusteringSession =>
      val clusteringSessionId: Int = clusteringSession.clusteringSessionId
      val routeId: Int = clusteringSession.routeId
      val clustering_threshold: Double = clusteringSession.clustering_threshold
      val time_created: java.sql.Timestamp = clusteringSession.time_created
      val deleted: Boolean = clusteringSession.deleted
      Json.obj("clusteringSessionId" -> clusteringSessionId, "routeId" -> routeId,
               "clustering_threshold" -> clustering_threshold, "time_created" -> time_created, "deleted" -> deleted)
    }
    val sessionCollection = Json.obj("sessions" -> ses)
    Future.successful(Ok(sessionCollection))
  }

  /**
    * Returns the set of labels associated with the given routeId and hitId
    * TODO figure out how to take in an Int for routeId (got a compilation error in conf/routes
    *
    * @param routeId
    * @param hitId
    * @return
    */
  def getLabelsToCluser(routeId: String, hitId: String) = UserAwareAction.async {implicit request =>
//    if (isAdmin(request.identity)) {
      val labsToCluster: List[LabelToCluster] = ClusteringSessionTable.getLabelsToCluser(routeId.toInt, hitId)
      val json = Json.arr(labsToCluster.map(x => Json.obj(
        "label_id" -> x.labelId, "label_type" -> x.labelType, "lat" -> x.lat, "lng" -> x.lng, "severity" -> x.severity,
        "temporary" -> x.temp, "turker_id" -> x.turkerId
      )))
      Future.successful(Ok(json))
//    } else {
//      Future.successful(Redirect("/"))
//    }
  }

  /**
    * Takes in results of clustering, and adds the data to the relevant tables
    */
  def postClusteringResults(routeId: String, threshold: String) = UserAwareAction.async(BodyParsers.parse.json) {implicit request =>
    // Validation https://www.playframework.com/documentation /2.3.x/ScalaJson
    val submission = request.body.validate[List[Lab]]
    submission.fold(
      errors => {
        println("bleepbloop how does parse")
        Future.successful(BadRequest(Json.obj("status" -> "Error", "message" -> JsError.toFlatJson(errors))))
      },
      submission => {
        val now = new DateTime(DateTimeZone.UTC)
        val timestamp: Timestamp = new Timestamp(now.getMillis)
        val sessionId: Int = ClusteringSessionTable.save(ClusteringSession(0, routeId.toInt, threshold.toDouble, timestamp, deleted = false))
        submission.groupBy(_.clusterNum).map { case (clust, labels) =>
            val clustId: Int = ClusteringSessionClusterTable.save(ClusteringSessionCluster(0, sessionId))
            for (label <- labels) yield {
              ClusteringSessionLabelTable.save(ClusteringSessionLabel(0, clustId, label.labelId))
            }
        }
      }
    )
    val json = Json.obj()
    println()
    Future.successful(Ok(json))
  }

}