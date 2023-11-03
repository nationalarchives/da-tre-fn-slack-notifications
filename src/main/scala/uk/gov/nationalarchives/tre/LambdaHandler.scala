package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import MessageParsingUtils._
import io.circe.syntax.EncoderOps
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.sys.env

class LambdaHandler() extends RequestHandler[SNSEvent, Unit] {

  override def handleRequest(event: SNSEvent, context: Context): Unit = {
    val webhookUrl = env("SLACK_WEBHOOK_URL")
    val environment = env("ENV")
    val channel = env("SLACK_CHANNEL")
    val username = env("SLACK_USERNAME")
    val defaults = SlackDefaults(channel, username)

    event.getRecords.asScala.toList match {
      case snsRecord :: Nil =>
        val messageString = snsRecord.getSNS.getMessage
        context.getLogger.log(s"Received message: $messageString\n")
        
        val slackMessage: Map[String, String] = parseGenericMessage(messageString).properties.messageType match {
          case "uk.gov.nationalarchives.tre.messages.bag.validate.BagValidate" => {
            val bagValidateMessage = parseBagValidate(messageString)
            buildSlackMessage(
              icon = ":wrench:",
              reference = bagValidateMessage.parameters.reference,
              messageType = bagValidateMessage.properties.messageType,
              environment = environment
            )
          }
          case "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse" => {
            val requestCourtDocumentParseMessage = parseRequestCourtDocumentParse(messageString)
            buildSlackMessage(
              icon = ":wrench:",
              reference = requestCourtDocumentParseMessage.parameters.reference,
              messageType = requestCourtDocumentParseMessage.properties.messageType,
              environment = environment
            )
          }
          case "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable" => {
            val courtDocumentPackageAvailableMessage = parseCourtDocumentPackageAvailable(messageString)
            buildSlackMessage(
              icon = ":wrench:",
              reference = courtDocumentPackageAvailableMessage.parameters.reference,
              messageType = courtDocumentPackageAvailableMessage.properties.messageType,
              status = Some(courtDocumentPackageAvailableMessage.parameters.status.toString),
              environment = environment
            )
          }
          case _ => 
            buildSlackMessage(
              icon = ":question",
              reference = "unknown",
              messageType = "unknown",
              environment = environment
            )
        }
        val httpClient = HttpClients.createDefault()
        val post = new HttpPost(webhookUrl)
        post.setEntity(new StringEntity(slackMessage.asJson.toString()))
        val response = httpClient.execute(post)
        println(Map("message" -> slackMessage, "status_code" -> response.getStatusLine.getStatusCode, "response" -> response.getEntity.getContent))
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }

    def buildSlackMessage(
      icon: String,
      reference: String,
      messageType: String,                   
      status: Option[String] = None,
      environment: String
    ): Map[String, String] = {
      val message = s"$icon Testing: $messageType message${status.map(s => s" with status $s")} received for reference $reference in environment $environment"
      Map(
        "channel" -> defaults.channel,
        "username" -> defaults.username,
        "text" -> message,
      )
    }
  }
  case class SlackDefaults(channel: String, username: String)
}