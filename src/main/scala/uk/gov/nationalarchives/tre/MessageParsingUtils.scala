package uk.gov.nationalarchives.tre

import io.circe.generic.auto._
import io.circe.{Decoder, parser}
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.bag.available.{BagAvailable, ConsignmentType}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Status => CDPAStatus}
import uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse
import uk.gov.nationalarchives.tre.messages.treerror.{TreError, Status => TEStatus}

object MessageParsingUtils {
  implicit val producerDecoder: Decoder[Producer.Value] = Decoder.decodeEnumeration(Producer)
  implicit val consignmentTypeDecoder: Decoder[ConsignmentType.Value] = Decoder.decodeEnumeration(ConsignmentType)
  implicit val courtDocumentPackageAvailableStatusDecoder: Decoder[CDPAStatus.Value] = Decoder.decodeEnumeration(CDPAStatus)
  implicit val treErrorStatusDecoder: Decoder[TEStatus.Value] = Decoder.decodeEnumeration(TEStatus)

  def parseGenericMessage(message: String): GenericMessage =
    parser.decode[GenericMessage](message).fold(error => throw new RuntimeException(error), identity)

  def parseBagAvailable(message: String): BagAvailable =
    parser.decode[BagAvailable](message).fold(error => throw new RuntimeException(error), identity)

  def parseRequestCourtDocumentParse(message: String): RequestCourtDocumentParse =
    parser.decode[RequestCourtDocumentParse](message).fold(error => throw new RuntimeException(error), identity)

  def parseCourtDocumentPackageAvailable(message: String): CourtDocumentPackageAvailable =
    parser.decode[CourtDocumentPackageAvailable](message).fold(error => throw new RuntimeException(error), identity)
    
  def parseTreError(message: String): TreError =
    parser.decode[TreError](message).fold(error => throw new RuntimeException(error), identity)
}
case class GenericMessage(properties: Properties)