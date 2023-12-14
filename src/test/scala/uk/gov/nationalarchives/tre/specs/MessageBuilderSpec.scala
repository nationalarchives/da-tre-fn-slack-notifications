package uk.gov.nationalarchives.tre.specs

import org.scalatest.flatspec._
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.Status.{COURT_DOCUMENT_PARSE_NO_ERRORS, COURT_DOCUMENT_PARSE_WITH_ERRORS}
import uk.gov.nationalarchives.tre.MessageBuilder._
import uk.gov.nationalarchives.tre.{Completed, CompletedWithErrors, Errored, Received, SlackMessageData}

class MessageBuilderSpec extends AnyFlatSpec with MockitoSugar {

  val environment = "test"

  "generateSlackMessageData" should "return the correct SlackMessageData for a BagAvailable message " in {
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

    generateSlackMessageData(testMessage, environment) shouldBe Some(
      SlackMessageData(
        messageProperties = Properties(
          messageType = "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable",
          timestamp = "2023-11-06T15:15:08.443071Z",
          function = "",
          producer = Producer.TDR,
          executionId = "",
          parentExecutionId = None
        ),
        reference = Some("TDR-2021-CF6L"),
        environment = environment,
        originator = Some("TDR"),
        errorMessage = None,
        requestStatus = Received,
        packageStatus = None
      )
    )
  }

  "it" should "return no SlackMessageData if the BagAvailable message has a standard consignment type" in {
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
        |	      "consignmentType" : "STANDARD",
        |	    	"s3Bucket" : "da-transform-sample-data",
        |	      "s3BagKey" : "dc34c025-ca5c-4746-b89a-a05bb451d344/sample-data/judgment/tdr-bag/TDR-2021-CF6L.tar.gz",
        |	      "s3BagSha256Key" : "TDR-2021-CF6L.tar.gz.sha256"
        |	  }
        |}
        |""".stripMargin

    generateSlackMessageData(testMessage, environment) shouldBe None
  }


  it should "return no SlackMessageData for a RequestCourtDocumentParse message with an originator of FCL" in {
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

    generateSlackMessageData(testMessage, environment) shouldBe None
  }

  it should "return no SlackMessageData for a RequestCourtDocumentParse message with an originator of TDR" in {
    val testMessage =
      """
        |{
        |		 "properties" : {
        |		    "messageType" : "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse",
        |		    "timestamp" : "2023-11-03T10:44:59.235436Z",
        |		    "function" : "",
        |		    "producer" : "TDR",
        |		    "executionId" : "",
        |		    "parentExecutionId" : null
        |		},
        |		    "parameters" : {
        |		    "reference" : "TDR-12345",
        |		    "s3Bucket" : "da-transform-sample-data",
        |		    "originator" : "TDR",
        |		    "parserInstructions" : {
        |		        "documentType" : "judgment"
        |	  	  },
        |		    "s3Key" : "e7ef7e6d-4cbc-4594-a68e-aab1b08f7498/sample-data/judgment/fcl-docx/eat_2022_1.docx"
        |	  }
        |	}
        |""".stripMargin

    generateSlackMessageData(testMessage, environment) shouldBe None
  }

  it should "return the correct SlackMessageData for a CourtDocumentPackageAvailable message without errors with an originator of TDR" in {
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

    generateSlackMessageData(testMessage, environment) shouldBe Some(
      SlackMessageData(
        messageProperties = Properties(
          messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
          function = "pte-ah-tre-court-document-pack-lambda",
          producer = Producer.TRE,
          executionId = "2443a118-f960-46f0-91e8-64baa1c9c84b",
          parentExecutionId = Some("5366065d-83e6-45d2-8fd5-0386abbe16f7"),
          timestamp = "2023-11-03T10:46:37.285978Z"
        ),
        reference = Some("TDR-2021-CF6L"),
        environment = environment,
        originator = Some("TDR"),
        errorMessage = None,
        requestStatus = Completed,
        packageStatus = Some(COURT_DOCUMENT_PARSE_NO_ERRORS)
      )
    )
  }

  it should "return the correct SlackMessageData for a CourtDocumentPackageAvailable message with errors with an originator of TDR" in {
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
        |        "status": "COURT_DOCUMENT_PARSE_WITH_ERRORS",
        |        "reference": "TDR-2021-CF6L",
        |        "s3Bucket": "pte-ah-tre-court-document-pack-out",
        |        "s3Key": "TDR-2021-CF6L/2443a118-f960-46f0-91e8-64baa1c9c84b/TRE-TDR-2021-CF6L.tar.gz",
        |        "metadataFilePath": "/metadata.json",
        |        "metadataFileType": "Json",
        |        "originator": "TDR"
        |    }
        |}
        |""".stripMargin

    generateSlackMessageData(testMessage, environment) shouldBe Some(
      SlackMessageData(
        messageProperties = Properties(
          messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
          function = "pte-ah-tre-court-document-pack-lambda",
          producer = Producer.TRE,
          executionId = "2443a118-f960-46f0-91e8-64baa1c9c84b",
          parentExecutionId = Some("5366065d-83e6-45d2-8fd5-0386abbe16f7"),
          timestamp = "2023-11-03T10:46:37.285978Z"
        ),
        reference = Some("TDR-2021-CF6L"),
        environment = environment,
        originator = Some("TDR"),
        errorMessage = None,
        requestStatus = CompletedWithErrors,
        packageStatus = Some(COURT_DOCUMENT_PARSE_WITH_ERRORS)
      )
    )
  }

  it should "return no SlackMessageData for a CourtDocumentPackageAvailable message without errors with an originator of FCL" in {
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
        |        "originator": "FCL"
        |    }
        |}
        |""".stripMargin

    generateSlackMessageData(testMessage, environment) shouldBe None
  }

  it should "return the correct SlackMessageData for a CourtDocumentPackageAvailable message with errors with an originator of FCL" in {
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
        |        "status": "COURT_DOCUMENT_PARSE_WITH_ERRORS",
        |        "reference": "TDR-2021-CF6L",
        |        "s3Bucket": "pte-ah-tre-court-document-pack-out",
        |        "s3Key": "TDR-2021-CF6L/2443a118-f960-46f0-91e8-64baa1c9c84b/TRE-TDR-2021-CF6L.tar.gz",
        |        "metadataFilePath": "/metadata.json",
        |        "metadataFileType": "Json",
        |        "originator": "FCL"
        |    }
        |}
        |""".stripMargin

    generateSlackMessageData(testMessage, environment) shouldBe Some(
      SlackMessageData(
        messageProperties = Properties(
          messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
          function = "pte-ah-tre-court-document-pack-lambda",
          producer = Producer.TRE,
          executionId = "2443a118-f960-46f0-91e8-64baa1c9c84b",
          parentExecutionId = Some("5366065d-83e6-45d2-8fd5-0386abbe16f7"),
          timestamp = "2023-11-03T10:46:37.285978Z"
        ),
        reference = Some("TDR-2021-CF6L"),
        environment = environment,
        originator = Some("FCL"),
        errorMessage = None,
        requestStatus = CompletedWithErrors,
        packageStatus = Some(COURT_DOCUMENT_PARSE_WITH_ERRORS)
      )
    )
  }

  it should "return the correct SlackMessageData for a TREError message" in {
    val testMessage =
      """
        |{
        |   "properties" : {
        |     "messageType" : "uk.gov.nationalarchives.tre.messages.treerror.TreError",
        |     "timestamp" : "2023-11-06T17:09:49.220693Z",
        |     "function" : "da-tre-fn-failure-destination",
        |		  "producer" : "TRE",
        |	    "executionId" : "c6ffcc06-fa82-4f84-8a25-3c30136cd499",
        |	    "parentExecutionId" : null
        |   },
        |	  "parameters" : {
        |     "status" : "TRE_ERROR",
        |	    "originator" : null,
        |     "reference" : "",
        |	    "errors" : "\nArrived at failure destination with error details:\n\n{errorMessage=Something has gone terribly wrong, errorType=java.lang.Exception, stackTrace=[uk.gov.nationalarchives.tre.Lambda.handleRequest(Lambda.scala:15), java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method), java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source), java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source), java.base/java.lang.reflect.Method.invoke(Unknown Source)]}\n"
        |	  }
        |}
        |""".stripMargin

    generateSlackMessageData(testMessage, environment) shouldBe Some(
      SlackMessageData(
        messageProperties = Properties(
          messageType = "uk.gov.nationalarchives.tre.messages.treerror.TreError",
          timestamp = "2023-11-06T17:09:49.220693Z",
          function = "da-tre-fn-failure-destination",
          producer = Producer.TRE,
          executionId = "c6ffcc06-fa82-4f84-8a25-3c30136cd499",
          parentExecutionId = None
        ),
        reference = None,
        environment = environment,
        originator = None,
        errorMessage = Some("\nArrived at failure destination with error details:\n\n{errorMessage=Something has gone terribly wrong, errorType=java.lang.Exception, stackTrace=[uk.gov.nationalarchives.tre.Lambda.handleRequest(Lambda.scala:15), java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method), java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(Unknown Source), java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source), java.base/java.lang.reflect.Method.invoke(Unknown Source)]}\n"),
        requestStatus = Errored,
        packageStatus = None
      )
    )
  }

  "formatTimestamp" should "correctly parse a zoned timestamp" in {
    formatTimestamp("2023-11-06T17:09:49.220693Z") shouldBe "17:09:49"
  }

  "formatTimestamp" should "correctly parse a non ISO timestamp of the format sent by TDR" in {
    formatTimestamp("2023-11-08 14:02:24.628853") shouldBe "14:02:24"
  }

  "formatTimestamp" should "return timestamp parse failed as string in case of being unable to parse" in {
    formatTimestamp("i am not a timestamp") shouldBe "timestamp parse failed"
  }
}
