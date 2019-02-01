package controllers

import auth.AuthActions
import com.gu.googleauth.GoogleGroupChecker
import config.AmiableConfigProvider
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, ControllerComponents, BaseController}

import scala.concurrent.ExecutionContext


class Login (val controllerComponents: ControllerComponents, override val amiableConfigProvider: AmiableConfigProvider, override val wsClient: WSClient)
                     (implicit exec: ExecutionContext) extends BaseController with AuthActions {

  val requiredGroups: Set[String] = amiableConfigProvider.requiredGoogleGroups
  val googleGroupChecker: GoogleGroupChecker = amiableConfigProvider.googleGroupChecker

  def loginError: Action[AnyContent] = Action { request =>
    val error = request.flash.get("error")
    Ok(views.html.loginError(error))
  }

  /*
   * Redirect to Google with anti forgery token (that we keep in session storage - note that flashing is NOT secure).
   */
  def startLogin: Action[AnyContent] = Action.async { implicit request =>
    startGoogleLogin()
  }

  /*
   * Looks up user's identity via Google and (optionally) enforces required Google groups at login time.
   *
   * To re-check Google group membership on every page request you can use the `requireGroup` filter
   * (see `Application.scala`).
   */
  def oauth2Callback: Action[AnyContent] = Action.async { implicit request =>
    processOauth2Callback(requiredGroups, googleGroupChecker)
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.Login.loggedOut()).withNewSession
  }

  def loggedOut: Action[AnyContent] = Action {
    Ok(views.html.loggedOut())
  }
}
