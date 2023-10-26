Feature: This lambda function will handle an SNS event and log its message

  Scenario: An SNS event is handled by the lambda handler
    When an SNS event is received with message data
    """
    {
        "properties": {
          "messageType": "uk.gov.nationalarchives.tre.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable"
        },
        "parameters": {
           "status": "COURT_DOCUMENT_PARSE_NO_ERRORS",
           "reference": "TRE-TEST",
           "s3Bucket": "test-tre-court-document-pack-out",
           "s3Key": "TRE-TEST/execution-id/TRE-TEST.tar.gz",
           "metadataFilePath": "/metadata.json",
           "metadataFileType": "Json"
        }
    }
    """

    Then an SNS event is logged with message data
    """
    {
        "properties": {
          "messageType": "uk.gov.nationalarchives.tre.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable"
        },
        "parameters": {
           "status": "COURT_DOCUMENT_PARSE_NO_ERRORS",
           "reference": "TRE-TEST",
           "s3Bucket": "test-tre-court-document-pack-out",
           "s3Key": "TRE-TEST/execution-id/TRE-TEST.tar.gz",
           "metadataFilePath": "/metadata.json",
           "metadataFileType": "Json"
        }
    }
    """
