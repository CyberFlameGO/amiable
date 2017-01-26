package controllers

import javax.inject.Inject

import auth.AuthActions
import config.AmiableConfigProvider
import models.{Attempt, SSA}
import play.api._
import play.api.mvc._
import prism.{Prism, PrismLogic, Recommendations}
import services.Agents

import scala.concurrent.ExecutionContext


class AMIable @Inject()(override val amiableConfigProvider: AmiableConfigProvider, agents: Agents)
                       (implicit exec: ExecutionContext) extends Controller with AuthActions {
  implicit val conf = amiableConfigProvider.conf

  def index = AuthAction.async { implicit request =>
    attempt {
      for {
        prodInstances <- Prism.instancesWithAmis(SSA(stage = Some("PROD")))
        oldInstances = PrismLogic.oldInstances(prodInstances)
        oldStacks = PrismLogic.stacks(oldInstances)
      } yield Ok(views.html.index(oldInstances, oldStacks.sorted, agents.oldProdInstanceCountHistory))
    }
  }

  def ami(imageId: String) = AuthAction.async { implicit request =>
    attempt {
      for {
        amis <- Prism.getAMIs()
        ami = amis.find(_.imageId == imageId)
        amiWithUpgrade = ami.map(Recommendations.amiWithUpgrade(agents.allAmis))
      } yield Ok(views.html.ami(amiWithUpgrade))
    }
  }

  def ssaInstanceAMIs(stackOpt: Option[String], stageOpt: Option[String], appOpt: Option[String]) = AuthAction.async { implicit request =>
    val ssa = SSA.fromParams(stackOpt, stageOpt, appOpt)
    attempt {
      for {
        (allAmis, amiSSAs) <- PrismLogic.ssaInstanceAmis(ssa, agents)
      } yield {
        Ok(views.html.instanceAMIs(ssa, allAmis, amiSSAs))
      }
    }
  }

  /**
    * `Attempt` with nicely formatted error handling using the error template
    */
  private def attempt[A](action: => Attempt[Result]) = {
    Attempt(action) { err =>
      Logger.error(err.logString)
      Status(err.statusCode)(views.html.error(err))
    }
  }
}
