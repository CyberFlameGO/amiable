package config

import java.io.FileInputStream
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.auth.oauth2.ServiceAccountCredentials
import com.gu.googleauth.{AntiForgeryChecker, GoogleAuthConfig, GoogleGroupChecker, GoogleServiceAccount}
import controllers.routes

import javax.inject.Inject
import play.api.Configuration
import play.api.http.HttpConfiguration
import play.api.libs.ws.WSClient

import scala.util.Try

case class AMIableConfig(
                          prismUrl: String,
                          amigoUrl: String,
                          wsClient: WSClient,
                          mailAddress: String,
                          ownerNotificationCron: Option[String],
                          overrideToAddress: Option[String],
                          amiableUrl: String
                        )

case class AuthConfig(googleAuthConfig: GoogleAuthConfig, googleGroupChecker: GoogleGroupChecker, requiredGoogleGroups: Set[String])

class AmiableConfigProvider @Inject() (val ws: WSClient, val playConfig: Configuration, val httpConfiguration: HttpConfiguration) {

  val amiableUrl: String = requiredString(playConfig, "host")

  val conf = AMIableConfig(
    playConfig.get[String]("prism.url"),
    playConfig.get[String]("amigo.url"),
    ws,
    playConfig.get[String]("amiable.mailClient.fromAddress"),
    playConfig.getOptional[String]("amiable.owner.notification.cron"),
    playConfig.getOptional[String]("amiable.owner.notification.overrideToAddress"),
    amiableUrl
  )

  val stage: String = requiredString(
    playConfig, "stage"
  )

  val requiredGoogleGroups: Set[String] = Set(requiredString(playConfig, "auth.google.2faGroupId"))

  val googleAuthConfig: GoogleAuthConfig = {

    // Different constructors depending on whether domain is available or not

    playConfig.getOptional[String]("auth.google.apps-domain").map(domain => {
      GoogleAuthConfig(
        clientId = requiredString(playConfig, "auth.google.clientId"),
        clientSecret = requiredString(playConfig, "auth.google.clientSecret"),
        redirectUrl = s"$amiableUrl${routes.Login.oauth2Callback.url}",
        domains = List(domain),
        antiForgeryChecker = AntiForgeryChecker.borrowSettingsFromPlay(httpConfiguration)
      )
    }).getOrElse(
      GoogleAuthConfig.withNoDomainRestriction(
        clientId = requiredString(playConfig, "auth.google.clientId"),
        clientSecret = requiredString(playConfig, "auth.google.clientSecret"),
        redirectUrl = s"$amiableUrl${routes.Login.oauth2Callback.url}",
        antiForgeryChecker = AntiForgeryChecker.borrowSettingsFromPlay(httpConfiguration)
      )
    )

  }

  val googleGroupChecker: GoogleGroupChecker = {
    val serviceAccountCertPath = requiredString(playConfig, "auth.google.serviceAccountCertPath")
    val creds = ServiceAccountCredentials.fromStream(new FileInputStream(serviceAccountCertPath))

    new GoogleGroupChecker(
      impersonatedUser = requiredString(playConfig, "auth.google.2faUser"),
      serviceAccountCredentials = creds,
    )
  }

  private def requiredString(config: Configuration, key: String): String = {
    config.getOptional[String](key).getOrElse {
      throw new RuntimeException(s"Missing required config property $key")
    }
  }

}
