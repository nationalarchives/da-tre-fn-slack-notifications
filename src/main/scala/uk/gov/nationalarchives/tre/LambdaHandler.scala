package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.jdk.CollectionConverters.CollectionHasAsScala


class LambdaHandler() extends RequestHandler[SNSEvent, Unit] {

  override def handleRequest(event: SNSEvent, context: Context): Unit = {
    // 1. ValidateBag - post entry notification
    // 2. RequestCourtDocumentParse - IF originator == FCL, post entry notification
    // 3. CourtDocumentPackageAvailable - post exit notification
    // 4. TREError - post error notification
    event.getRecords.asScala.toList match {
      case snsRecord :: Nil =>
        context.getLogger.log(s"Received message: ${snsRecord.getSNS.getMessage}\n")
        //val messageType = MessageParsingUtils.parseGenericMessage(snsRecord.getSNS.getMessage).properties.messageType
        println(s"Message type: testing")
      case _ => throw new RuntimeException("Single record expected; zero or multiple received")
    }
  }
}