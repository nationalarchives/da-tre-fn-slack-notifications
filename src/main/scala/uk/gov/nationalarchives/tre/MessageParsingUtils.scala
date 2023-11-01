package uk.gov.nationalarchives.tre

import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, parser}
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Status}
import uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse
import uk.gov.nationalarchives.tre.messages.bag.validate.BagValidate

object MessageParsingUtils {
  implicit val producerDecoder: Decoder[Producer.Value] = Decoder.decodeEnumeration(Producer)
  
  def parseGenericMessage(message: String): GenericMessage =
    parser.decode[GenericMessage](message).fold(error => throw new RuntimeException(error), identity)

  def parseBagValidate(message: String): BagValidate =
    parser.decode[BagValidate](message).fold(error => throw new RuntimeException(error), identity)

  def parseRequestCourtDocumentParse(message: String): RequestCourtDocumentParse =
    parser.decode[RequestCourtDocumentParse](message).fold(error => throw new RuntimeException(error), identity)

  def parseCourtDocumentPackageAvailable(message: String): CourtDocumentPackageAvailable =
    parser.decode[CourtDocumentPackageAvailable](message).fold(error => throw new RuntimeException(error), identity)
}
case class GenericMessage(properties: Properties)