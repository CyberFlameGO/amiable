package controllers

import auth.AuthActions
import com.google.inject.Inject
import config.AmiableConfigProvider
import models.Attempt
import personalisation.DashboardLogic
import play.api.mvc.{Controller, MultipartFormData}
import play.api.data._
import play.api.data.Forms._


class Dashboard @Inject()(override val amiableConfigProvider: AmiableConfigProvider) extends Controller with AuthActions {
  def addSSA = AuthAction(parse.urlFormEncoded) { implicit request =>
    DashboardLogic.addSSA(request).fold(
      err => BadRequest(err),
      cookie => SeeOther("/").withCookies(cookie)
    )
  }
}
