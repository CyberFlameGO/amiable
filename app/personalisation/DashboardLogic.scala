package personalisation

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.gu.googleauth.UserIdentity
import models.Serialisation._
import models._
import play.api.Logger
import play.api.libs.json.{JsResultException, Json}
import play.api.mvc.{Cookie, RequestHeader, Security}


object DashboardLogic {
  private val cookieName = "amiable-dashboard-prefs"

  def addSSA(request: Security.AuthenticatedRequest[Map[String, scala.Seq[String]], UserIdentity]): Either[String, Cookie] = {
    val cookieOrError = ssaFromPost(request.body) match {
      case Some(ssa) =>
        for {
          currentPrefs <- fromRequest(request).right
        } yield {
          val updatedPrefs = addSSAToPrefs(currentPrefs, ssa)
          asCookie(updatedPrefs)
        }
      case None =>
        Left("Invalid Stack/Stage/App combination")
    }
  }

  def addSSAToPrefs(prefs: DashboardPrefs, ssa: SSA): DashboardPrefs = {
    if(prefs.ssas.contains(ssa)) prefs
    else {
      prefs.copy(ssas = ssa :: prefs.ssas)
    }
  }

  def removeSSA(prefs: DashboardPrefs, ssa: SSA): DashboardPrefs = {
    prefs.copy(ssas = prefs.ssas.filter(_ == ssa))
  }

  def asCookie(prefs: DashboardPrefs): Cookie = {
    val value = Json.toJson(prefs).toString()
    Cookie(name = cookieName, value = value, maxAge = Some(86400 * 365), path = "/", secure = true, httpOnly = true)
  }

  def fromRequest(request: RequestHeader): Either[String, DashboardPrefs] = {
    request.cookies.find(_.name == cookieName) match {
      case Some(cookie) =>
        fromCookie(cookie)
      case None =>
        Logger.error("Could not extract DashboardPrefs from JSON")
        Left("Could not extract DashboardPrefs from JSON")
    }
  }

  private[personalisation] def fromCookie(cookie: Cookie): Either[String, DashboardPrefs] = {
    try {
      Right(Json.parse(cookie.value).as[DashboardPrefs])
    } catch {
      case e: JsResultException =>
        Logger.error("Could not extract DashboardPrefs from JSON", e)
        Left("Could not extract DashboardPrefs from JSON")
      case e: JsonParseException =>
        Logger.error("Invalid DashboardPrefs cookie data", e)
        Left("Invalid DashboardPrefs cookie data")
      case e: JsonMappingException =>
        Logger.error("Invalid DashboardPrefs JSON", e)
        Left("Invalid DashboardPrefs JSON")
    }
  }

  def ssaFromPost(body: Map[String, scala.Seq[String]]): Option[SSA] = {
    val keys = Set("stack", "stage", "app")
    if(body.keySet.intersect(keys) == keys) Some {
      SSA.fromParams(
        stack = body.get("stack").flatMap(_.headOption),
        stage = body.get("stage").flatMap(_.headOption),
        app = body.get("app").flatMap(_.headOption)
      )
    } else None
  }
}
