package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import MessageParsingUtils._
import org.apache.http.impl.client.HttpClients
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.sys.env
import MessageBuilder._

class LambdaHandler() extends RequestHandler[SNSEvent, Unit] {

  override def handleRequest(event: SNSEvent, context: Context): Unit = {
    val webhookUrl = env("SLACK_WEBHOOK_URL")
    val environment = env("ENV")
    val channel = env("SLACK_CHANNEL")
    val username = env("SLACK_USERNAME")
    val notifiableSlackEndpointsOnError = parseStringMap(env("NOTIFIABLE_SLACK_ENDPOINTS_ON_ERROR"))
    val httpClient = HttpClients.createDefault()
    val slackUtils = new SlackUtils(httpClient)

    event.getRecords.asScala.toList match {
      case snsRecord :: Nil =>
        val messageString = snsRecord.getSNS.getMessage
        context.getLogger.log(s"Received message: $messageString\n")
        val slackMessageData = generateSlackMessageData(messageString, environment)
        slackMessageData.foreach { data =>
          val messageText = buildMessageText(data)
          slackUtils.postMessage(webhookUrl, messageText, channel, username)
          if (data.requestStatus == Errored) notifiableSlackEndpointsOnError
            .foreach { case (c, wh) => slackUtils.postMessage(wh, messageText, c, username) }
        }
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }
}
