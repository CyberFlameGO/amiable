import config.AmiableConfigProvider
import controllers.{AMIable, Healthcheck, Login}
import play.api.ApplicationLoader.Context
import play.api._
import _root_.controllers.AssetsComponents
import play.api.i18n.DefaultLangs
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.{AhcWSClientProvider, AsyncHttpClientProvider}
import play.filters.HttpFiltersComponents

//import play.api.routing.Router.Routes

//import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.logback.LogbackLoggerConfigurator
import play.api.libs.ws.ahc.AhcWSComponents
import router.Routes
import services.notification.AmazonSimpleEmailServiceAsyncFactory._
import services.notification.{AWSMailClient, Notifications, ScheduledNotificationRunner}
import services.{Agents, Metrics}

class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = {

    new LogbackLoggerConfigurator().configure(context.environment)

    println(
      """

                                                         .---.
                  __  __   ___   .--.          /|        |   |      __.....__
                 |  |/  `.'   `. |__|          ||        |   |  .-''         '.
                 |   .-.  .-.   '.--.          ||        |   | /     .-''"'-.  `.
            __   |  |  |  |  |  ||  |    __    ||  __    |   |/     /________\   \
         .:--.'. |  |  |  |  |  ||  | .:--.'.  ||/'__ '. |   ||                  |
        / |   \ ||  |  |  |  |  ||  |/ |   \ | |:/`  '. '|   |\    .-------------'
        `" __ | ||  |  |  |  |  ||  |`" __ | | ||     | ||   | \    '-.____...---.
         .'.''| ||__|  |__|  |__||__| .'.''| | ||\    / '|   |  `.             .'
        / /   | |_                   / /   | |_|/\'..' / '---'    `''-...... -'
        \ \._,\ '/                   \ \._,\ '/'  `'-'`
         `--'  `"                     `--'  `"
      """)

    val components = new AppComponents(context)
    components.application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with AssetsComponents with HttpFiltersComponents {

  override lazy val environment: play.api.Environment = context.environment
  override lazy val configuration = context.initialConfiguration

  val agents = new Agents(amiableConfigProvider, applicationLifecycle, actorSystem, context.environment)
  val metrics = new Metrics(context.environment, agents, applicationLifecycle)

  lazy val wsClient: WSClient = {
    //implicit val mat = materializer
    //implicit val ec = executionContext
    val asyncHttpClient = new AsyncHttpClientProvider(context.environment, context.initialConfiguration, applicationLifecycle).get
    new AhcWSClientProvider(asyncHttpClient).get
  }

  lazy val amiableConfigProvider = new AmiableConfigProvider(wsClient, context.initialConfiguration)

  lazy val awsMailClient = new AWSMailClient(amazonSimpleEmailServiceAsync)
  lazy val scheduledNotificationRunner = new ScheduledNotificationRunner(awsMailClient, context.environment, amiableConfigProvider)
  lazy val notifications = new Notifications(amiableConfigProvider, context.environment, applicationLifecycle, scheduledNotificationRunner)

  lazy val amiableController = new AMIable(amiableConfigProvider, agents, notifications)(executionContext)

  lazy val healthCheckController = new Healthcheck
  lazy val loginController = new Login(amiableConfigProvider, wsClient)(executionContext)

  lazy val router = new Routes(httpErrorHandler, amiableController, healthCheckController, loginController, assets)

}
