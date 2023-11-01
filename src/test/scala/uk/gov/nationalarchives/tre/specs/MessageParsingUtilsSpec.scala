package uk.gov.nationalarchives.tre.specs

import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.scalatest.flatspec._
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Parameters, Status}
import uk.gov.nationalarchives.tre.GenericMessage
import uk.gov.nationalarchives.tre.MessageParsingUtils._

class MessageParsingUtilsSpec extends AnyFlatSpec with MockitoSugar {

  "The message parsing utils" should "parse a valid tre message" in {
    val testMessage =
      """
        |{
        |    "properties": {
        |        "messageType": "uk.gov.nationalarchives.tre.messages.courtdocument.parse.CourtDocumentParse",
        |        "function": "tre-tf-module-parse-judgment",
        |        "producer": "TRE",
        |        "executionId": "b0d36170-3733-4a6c-a2d3-429ad700849f",
        |        "parentExecutionId": "",
        |        "timestamp": "2023-10-31T16:12:19.340Z"
        |    },
        |    "parameters": {
        |        "reference": "FCL-12345",
        |        "s3FolderName": "court-documents/FCL-12345/b0d36170-3733-4a6c-a2d3-429ad700849f",
        |        "originator": "FCL",
        |        "s3Bucket": "pte-ah-tre-common-data",
        |        "status": "COURT_DOCUMENT_PARSE_NO_ERRORS"
        |    }
        |}
        |""".stripMargin

    parseGenericMessage(testMessage) shouldBe GenericMessage(
      properties = Properties(
        messageType = "uk.gov.nationalarchives.tre.messages.courtdocument.parse.CourtDocumentParse",
        function = "tre-tf-module-parse-judgment",
        producer = Producer.TRE,
        executionId = "b0d36170-3733-4a6c-a2d3-429ad700849f",
        parentExecutionId = Some(""),
        timestamp = "2023-10-31T16:12:19.340Z"
      )
    )
  }
}
