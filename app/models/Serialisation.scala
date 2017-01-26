package models

import play.api.libs.json.Json

object Serialisation {
  import datetime.DateUtils._

  implicit val originFormat = Json.format[Origin]
  implicit val metaFormat = Json.format[Meta]
  implicit val instanceFormat = Json.format[Instance]
  implicit val ssaFormat = Json.format[SSA]
  implicit val dashboardPrefsFormat = Json.format[DashboardPrefs]
}
