package personalisation

import models.{DashboardPrefs, SSA}
import org.scalatest.{EitherValues, FreeSpec, Matchers, OptionValues}
import util.AttemptValues

import scala.concurrent.ExecutionContext.Implicits.global


class DashboardLogicTest extends FreeSpec with Matchers with AttemptValues with EitherValues with OptionValues {
  val ssa1 = SSA(Some("stack1"), Some("stage1"), Some("app1"))
  val ssa2 = SSA(Some("stack2"), Some("stage2"), Some("app2"))
  val prefs = DashboardPrefs(List(ssa1, ssa2))

  "cookie conversions" - {
    "can do a full round trip on valid prefs" in {
      val parsedPrefs = DashboardLogic.fromCookie(DashboardLogic.asCookie(prefs)).awaitEither.right.value
      parsedPrefs shouldEqual prefs
    }

    "asCookie" - {
      "sets a nice long expiry time on the cookie" in {
        DashboardLogic.asCookie(prefs).maxAge.value should be >= 86400 * 365
      }
    }
  }

  "addSSA" - {
    "returns the same prefs if the SSA already exists" in {
      DashboardLogic.addSSA(prefs, ssa1) shouldEqual prefs
    }

    "returns a new prefs with the provded SSA included" in {
      val ssa3 = SSA(Some("stack3"), Some("stage3"), Some("app3"))
      DashboardLogic.addSSA(prefs, ssa3).ssas should contain(ssa3)
    }
  }
}
