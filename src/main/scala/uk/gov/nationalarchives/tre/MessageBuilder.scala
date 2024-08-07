package uk.gov.nationalarchives.tre

import uk.gov.nationalarchives.common.messages.Properties
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Status => PackageStatus}
import uk.gov.nationalarchives.da.messages.bag.available.ConsignmentType.STANDARD
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.Status.{COURT_DOCUMENT_PARSE_NO_ERRORS, COURT_DOCUMENT_PARSE_WITH_ERRORS}
import uk.gov.nationalarchives.tre.MessageParsingUtils.{parseBagAvailable, parseCourtDocumentPackageAvailable, parseGenericMessage, parseTreError}

import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.{Instant, LocalDateTime, ZoneId}


object MessageBuilder {
  def generateSlackMessageData(message: String, environment: String): Option[SlackMessageData] = {
    val messageProperties = parseGenericMessage(message).properties
    Option(messageProperties.messageType) collect {
      case "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable"
        if parseBagAvailable(message).parameters.consignmentType != STANDARD =>
        val bagAvailableMessage = parseBagAvailable(message)
        SlackMessageData(
          messageProperties = messageProperties,
          reference = Some(bagAvailableMessage.parameters.reference),
          environment = environment,
          originator = bagAvailableMessage.parameters.originator,
          errorMessage = None,
          requestStatus = Received,
          packageStatus = None
        )
      case "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable" =>
//        if isNotFclParseWithoutErrors(parseCourtDocumentPackageAvailable(message)) =>
        val courtDocumentPackageAvailableMessage = parseCourtDocumentPackageAvailable(message)
        val requestStatus = courtDocumentPackageAvailableMessage.parameters.status match {
          case COURT_DOCUMENT_PARSE_NO_ERRORS => Completed
          case COURT_DOCUMENT_PARSE_WITH_ERRORS => CompletedWithErrors
        }
        SlackMessageData(
          messageProperties = messageProperties,
          reference = Some(courtDocumentPackageAvailableMessage.parameters.reference),
          environment = environment,
          originator = courtDocumentPackageAvailableMessage.parameters.originator,
          errorMessage = None,
          requestStatus = requestStatus,
          packageStatus = Some(courtDocumentPackageAvailableMessage.parameters.status)
        )
      case "uk.gov.nationalarchives.tre.messages.treerror.TreError" =>
        val treErrorMessage = parseTreError(message)
        SlackMessageData(
          messageProperties = messageProperties,
          reference = None,
          environment = environment,
          originator = None,
          errorMessage = treErrorMessage.parameters.errors,
          requestStatus = Errored,
          packageStatus = None
        )
    }
  }

//  private def isNotFclParseWithoutErrors(courtDocumentPackageAvailableMessage: CourtDocumentPackageAvailable): Boolean = {
//    !(courtDocumentPackageAvailableMessage.parameters.originator.contains("FCL") &&
//      courtDocumentPackageAvailableMessage.parameters.status == COURT_DOCUMENT_PARSE_NO_ERRORS)
//  }

  def buildMessageText(messageData: SlackMessageData): String = {
    import messageData._
    val headerLine = s":${requestStatus.iconName}: *${requestStatus.header}* ${reference.map(r => s"(`$r`)").getOrElse("")}"
    val timeLine = s":stopwatch: `${formatTimestamp(messageProperties.timestamp)}`"
    val environmentLine = s"*Environment*: `$environment`"
    val typeLine = s"*Type*: `${messageProperties.messageType}`"
    val originatorLine = originator.map(o => s"*Originator*: `$o`").getOrElse("")
    val statusLine = packageStatus.map(s => s"*Status*: `${s.toString}`").getOrElse("")
    val errorMessageLine = errorMessage.map(e => s"*Error message*: ```$e```").getOrElse("")

    Seq(headerLine, timeLine, environmentLine, typeLine, originatorLine, statusLine, errorMessageLine)
      .filter(_.nonEmpty)
      .mkString("\n")
  }

  def formatTimestamp(timestampString: String) = {
    val customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    val londonZoneId = ZoneId.of("Europe/London")
    val outFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    try {
      if (timestampString.endsWith("Z") && timestampString.contains("T")) {
        val instant = Instant.parse(timestampString)
        val zonedTimestamp = instant.atZone(londonZoneId)
        zonedTimestamp.format(outFormatter)
      } else {
        val localDateTime = LocalDateTime.parse(timestampString, customFormatter)
        val zonedTimestamp = localDateTime.atZone(londonZoneId)
        zonedTimestamp.format(outFormatter)
      }
    } catch {
      case _: DateTimeParseException => "timestamp parse failed"
    }
  }
}

case class SlackMessageData(
   messageProperties: Properties,
   reference: Option[String],
   environment: String,
   originator: Option[String],
   errorMessage: Option[String],
   requestStatus: RequestStatus,
   packageStatus: Option[PackageStatus.Value]
)

sealed trait RequestStatus {
  val header: String
  val iconName: String
}

case object Received extends RequestStatus {
  val header = "Request Received"
  val iconName = "hourglass_flowing_sand"
}

case object Completed extends RequestStatus {
  val header = "Request Completed"
  val iconName = "white_check_mark"
}

case object CompletedWithErrors extends RequestStatus {
  val header = "Request Completed with Errors"
  val iconName = "warning"
}

case object Errored extends RequestStatus {
  val header = "TRE Error"
  val iconName = "interrobang"
}
