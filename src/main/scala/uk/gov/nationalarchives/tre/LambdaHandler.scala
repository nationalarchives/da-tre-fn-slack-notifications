package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import MessageParsingUtils._
import io.circe.syntax.EncoderOps
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.Status.{COURT_DOCUMENT_PARSE_NO_ERRORS, COURT_DOCUMENT_PARSE_WITH_ERRORS}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.sys.env

class LambdaHandler() extends RequestHandler[SNSEvent, Unit] {

  override def handleRequest(event: SNSEvent, context: Context): Unit = {
    val webhookUrl = env("SLACK_WEBHOOK_URL")
    val environment = env("ENV")
    val channel = env("SLACK_CHANNEL")
    val username = env("SLACK_USERNAME")
    val notifiableSlackEndpointsOnError = env("NOTIFIABLE_SLACK_ENDPOINTS_ON_ERROR")

    val defaults = SlackDefaults(channel, username)

    event.getRecords.asScala.toList match {
      case snsRecord :: Nil =>
        val messageString = snsRecord.getSNS.getMessage
        context.getLogger.log(s"Received message: $messageString\n")
        context.getLogger.log(s"Notifiable slack endpoints on error: $notifiableSlackEndpointsOnError\n")

        val slackMessage = parseGenericMessage(messageString).properties.messageType match {
          case "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable" => {
            val bagAvailableMessage = parseBagAvailable(messageString)
            val notifiable = buildSlackMessage(
              header = "Request Received",
              timestampString = bagAvailableMessage.properties.timestamp,
              icon = ":hourglass_flowing_sand:",
              reference = Some(bagAvailableMessage.parameters.reference),
              messageType = bagAvailableMessage.properties.messageType,
              environment = environment,
              originator = bagAvailableMessage.parameters.originator
            )
            Some(notifiable)
          }
          case "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse" => {
            val requestCourtDocumentParseMessage = parseRequestCourtDocumentParse(messageString)
            val notifiable = buildSlackMessage(
              header = "Request Received",
              timestampString = requestCourtDocumentParseMessage.properties.timestamp,
              icon = ":hourglass_flowing_sand:",
              reference = Some(requestCourtDocumentParseMessage.parameters.reference),
              messageType = requestCourtDocumentParseMessage.properties.messageType,
              environment = environment,
              originator = requestCourtDocumentParseMessage.parameters.originator
            )
            if (requestCourtDocumentParseMessage.parameters.originator.contains("FCL")) Some(notifiable) else None
          }
          case "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable" => {
            val courtDocumentPackageAvailableMessage = parseCourtDocumentPackageAvailable(messageString)
            val notifiable = courtDocumentPackageAvailableMessage.parameters.status match {
              case COURT_DOCUMENT_PARSE_NO_ERRORS => buildSlackMessage(
                header = "Request Completed",
                timestampString = courtDocumentPackageAvailableMessage.properties.timestamp,
                icon = ":white_check_mark:",
                reference = Some(courtDocumentPackageAvailableMessage.parameters.reference),
                messageType = courtDocumentPackageAvailableMessage.properties.messageType,
                environment = environment,
                status = Some(courtDocumentPackageAvailableMessage.parameters.status.toString)
              )
              case COURT_DOCUMENT_PARSE_WITH_ERRORS => buildSlackMessage(
                header = "Request Completed with Errors",
                timestampString = courtDocumentPackageAvailableMessage.properties.timestamp,
                icon = ":warning:",
                reference = Some(courtDocumentPackageAvailableMessage.parameters.reference),
                messageType = courtDocumentPackageAvailableMessage.properties.messageType,
                environment = environment,
                status = Some(courtDocumentPackageAvailableMessage.parameters.status.toString)
              )
            }
            Some(notifiable)
          }
          case "uk.gov.nationalarchives.tre.messages.treerror.TreError" => {
            val treErrorMessage = parseTreError(messageString)
            val notifiable = buildSlackMessage(
              header = "TRE Error",
              timestampString = treErrorMessage.properties.timestamp,
              icon = ":interrobang:",
              messageType = treErrorMessage.properties.messageType,
              environment = environment,
              errorMessage = treErrorMessage.parameters.errors
            )
            Some(notifiable)
          }
          case _ => None
        }
        slackMessage.foreach { message =>
          val httpClient = HttpClients.createDefault()
          val post = new HttpPost(webhookUrl)
          post.setEntity(new StringEntity(message.asJson.toString()))
          val response = httpClient.execute(post)
          println(Map("message" -> slackMessage, "status_code" -> response.getStatusLine.getStatusCode, "response" -> response.getEntity.getContent))
        }
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }

    def buildSlackMessage(
       header: String,
       timestampString: String,
       icon: String,
       reference: Option[String] = None,
       messageType: String,
       environment: String,
       status: Option[String] = None,
       errorMessage: Option[String] = None,
       originator: Option[String] = None                   
    ): Map[String, String] = {
      val instant = Instant.parse(timestampString)
      val zonedTimestamp = instant.atZone(ZoneId.of("Europe/London"))
      val formattedTime = zonedTimestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
      
      val headerLine = s"$icon *$header* ${reference.map(r => s"(`$r`)").getOrElse("")}"
      val timeLine = s":stopwatch: `$formattedTime`"
      val environmentLine = s"*Environment*: `$environment`"
      val typeLine = s"*Type*: `$messageType`"
      val originatorLine = originator.map(o => s"*Originator*: `$o`").getOrElse("")
      val statusLine = status.map(s => s"*Status*: `$s`").getOrElse("")
      val errorMessageLine = errorMessage.map(e => s"*Error message*: ```$e```").getOrElse("")

      val message =  Seq(headerLine, timeLine, environmentLine, typeLine, originatorLine, statusLine, errorMessageLine)
          .filter(_.nonEmpty)
          .mkString("\n")

      Map(
        "channel" -> defaults.channel,
        "username" -> defaults.username,
        "text" -> message,
        "icon" -> icon
      )
    }
  }
  case class SlackDefaults(channel: String, username: String)
}