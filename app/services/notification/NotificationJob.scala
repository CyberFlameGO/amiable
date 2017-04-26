package services.notification

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import config.AMIableConfig
import org.quartz.{Job, JobExecutionContext}

import scala.concurrent.ExecutionContext

/** Quartz job wrapper for [[ScheduledNotificationRunner]] */
class NotificationJob extends Job {

  override def execute(context: JobExecutionContext): Unit = {
    val schedulerContext = context.getScheduler.getContext
    val mailClient = schedulerContext.get("MailClient").asInstanceOf[AWSMailClient]
    implicit val config = schedulerContext.get("AMIableConfig").asInstanceOf[AMIableConfig]
    implicit val ec = schedulerContext.get("ExecutionContext").asInstanceOf[ExecutionContext]
    ScheduledNotificationRunner.run(mailClient)
  }
}
