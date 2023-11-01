package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable
import uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse
import uk.gov.nationalarchives.tre.messages.bag.validate.BagValidate
import MessageParsingUtils._
import uk.gov.nationalarchives.tre.messages.treerror.TreError

import scala.jdk.CollectionConverters.CollectionHasAsScala

class LambdaHandler() extends RequestHandler[SNSEvent, Unit] {

  override def handleRequest(event: SNSEvent, context: Context): Unit = {
    // 1. ValidateBag - post entry notification
    // 2. RequestCourtDocumentParse - IF originator == FCL, post entry notification
    // 3. CourtDocumentPackageAvailable - post exit notification
    // 4. TREError - post error notification
    event.getRecords.asScala.toList match {
      case snsRecord :: Nil =>
        val messageString = snsRecord.getSNS.getMessage
        context.getLogger.log(s"Received message: $messageString\n")
        
        parseGenericMessage(messageString).properties.messageType match {
          case "uk.gov.nationalarchives.tre.messages.bag.validate.BagValidate" => parseBagValidate(messageString)  
          case "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse" => 
            parseRequestCourtDocumentParse(messageString)
          case "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable" => 
            parseRequestCourtDocumentParse(messageString)
          case "uk.gov.nationalarchives.tre.messages.treerror.TreError" => TreError
        }
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }
}