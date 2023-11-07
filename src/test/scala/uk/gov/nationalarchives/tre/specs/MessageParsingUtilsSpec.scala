package uk.gov.nationalarchives.tre.specs

import org.scalatest.flatspec._
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Status, Parameters => CDPAParameters}
import uk.gov.nationalarchives.da.messages.request.courtdocument.parse.{ParserInstructions, RequestCourtDocumentParse, Parameters => RCDPParameters}
import uk.gov.nationalarchives.tre.GenericMessage
import uk.gov.nationalarchives.tre.MessageParsingUtils._
import uk.gov.nationalarchives.da.messages.bag.available.{BagAvailable, ConsignmentType, Parameters => BAParameters}

class MessageParsingUtilsSpec extends AnyFlatSpec with MockitoSugar {

  "The message parsing utils" should "parse the properties of any message using parse generic message" in {
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

  it should "parse a valid BagAvailable message " in {
    val testMessage =
      """
        |{
        |	  "properties" : {
        |	      "messageType" : "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable",
        |       "timestamp" : "2023-11-06T15:15:08.443071Z",
        |		    "function" : "",
        |	      "producer" : "TDR",
        |	      "executionId" : "",
        |       "parentExecutionId" : null
        |	  },
        |	  "parameters" : {
        |	      "reference" : "TDR-2021-CF6L",
        |	      "originator" : "TDR",
        |	      "consignmentType" : "COURT_DOCUMENT",
        |	    	"s3Bucket" : "da-transform-sample-data",
        |	      "s3BagKey" : "dc34c025-ca5c-4746-b89a-a05bb451d344/sample-data/judgment/tdr-bag/TDR-2021-CF6L.tar.gz",
        |	      "s3BagSha256Key" : "TDR-2021-CF6L.tar.gz.sha256"
        |	  }
        |}
        |""".stripMargin

    parseBagAvailable(testMessage) shouldBe BagAvailable(
      properties = Properties(
        messageType = "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable",
        timestamp = "2023-11-06T15:15:08.443071Z",
        function = "",
        producer = Producer.TDR,
        executionId = "",
        parentExecutionId = None
      ),
      parameters = BAParameters(
        reference = "TDR-2021-CF6L",
        consignmentType = ConsignmentType.COURT_DOCUMENT,
        originator = Some("TDR"),
        s3Bucket = "da-transform-sample-data",
        s3BagKey = "dc34c025-ca5c-4746-b89a-a05bb451d344/sample-data/judgment/tdr-bag/TDR-2021-CF6L.tar.gz",
        s3BagSha256Key = "TDR-2021-CF6L.tar.gz.sha256"
      )
    )
  }

  it should "parse a valid RequestCourtDocumentParse message" in {
    val testMessage =
      """
        |{
        |		 "properties" : {
        |		    "messageType" : "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse",
        |		    "timestamp" : "2023-11-03T10:44:59.235436Z",
        |		    "function" : "",
        |		    "producer" : "FCL",
        |		    "executionId" : "",
        |		    "parentExecutionId" : null
        |		},
        |		    "parameters" : {
        |		    "reference" : "FCL-12345",
        |		    "s3Bucket" : "da-transform-sample-data",
        |		    "originator" : "FCL",
        |		    "parserInstructions" : {
        |		        "documentType" : "judgment"
        |	  	  },
        |		    "s3Key" : "e7ef7e6d-4cbc-4594-a68e-aab1b08f7498/sample-data/judgment/fcl-docx/eat_2022_1.docx"
        |	  }
        |	}
        |""".stripMargin

    parseRequestCourtDocumentParse(testMessage) shouldBe RequestCourtDocumentParse(
      properties = Properties(
        messageType = "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse",
        timestamp = "2023-11-03T10:44:59.235436Z",
        function = "",
        producer = Producer.FCL,
        executionId = "",
        parentExecutionId = None,
      ),
      parameters = RCDPParameters(
        reference = "FCL-12345",
        s3Bucket = "da-transform-sample-data",
        originator = Some("FCL"),
        parserInstructions = ParserInstructions(
          documentType = "judgment"
        ),
        s3Key = "e7ef7e6d-4cbc-4594-a68e-aab1b08f7498/sample-data/judgment/fcl-docx/eat_2022_1.docx"
      )
    )
  }

  it should "parse a valid CourtDocumentPackageAvailable message" in {
    val testMessage =
      """
        |{
        |    "properties": {
        |        "messageType": "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
        |        "timestamp": "2023-11-03T10:46:37.285978Z",
        |        "function": "pte-ah-tre-court-document-pack-lambda",
        |        "producer": "TRE",
        |        "executionId": "2443a118-f960-46f0-91e8-64baa1c9c84b",
        |        "parentExecutionId": "5366065d-83e6-45d2-8fd5-0386abbe16f7"
        |    },
        |    "parameters": {
        |        "status": "COURT_DOCUMENT_PARSE_NO_ERRORS",
        |        "reference": "TDR-2021-CF6L",
        |        "s3Bucket": "pte-ah-tre-court-document-pack-out",
        |        "s3Key": "TDR-2021-CF6L/2443a118-f960-46f0-91e8-64baa1c9c84b/TRE-TDR-2021-CF6L.tar.gz",
        |        "metadataFilePath": "/metadata.json",
        |        "metadataFileType": "Json",
        |        "originator": "TDR"
        |    }
        |}
        |""".stripMargin

    parseCourtDocumentPackageAvailable(testMessage) shouldBe CourtDocumentPackageAvailable(
      properties = Properties(
        messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
        function = "pte-ah-tre-court-document-pack-lambda",
        producer = Producer.TRE,
        executionId = "2443a118-f960-46f0-91e8-64baa1c9c84b",
        parentExecutionId = Some("5366065d-83e6-45d2-8fd5-0386abbe16f7"),
        timestamp = "2023-11-03T10:46:37.285978Z"
      ),
      parameters = CDPAParameters(
        reference = "TDR-2021-CF6L",
        originator = Some("TDR"),
        s3Bucket = "pte-ah-tre-court-document-pack-out",
        s3Key = "TDR-2021-CF6L/2443a118-f960-46f0-91e8-64baa1c9c84b/TRE-TDR-2021-CF6L.tar.gz",
        metadataFilePath = "/metadata.json",
        metadataFileType = "Json",
        status = Status.COURT_DOCUMENT_PARSE_NO_ERRORS
      )
    )
  }

  it should "parse a json like env var with a notifiable channel/webhook pair" in {
    parseStringMap("""{"channel_name":"webhook_url"}""") shouldBe Map("channel_name" -> "webhook_url")
  }

  it should "parse an empty map of notifiables" in {
    parseStringMap("{}") shouldBe Map.empty
  }
}
