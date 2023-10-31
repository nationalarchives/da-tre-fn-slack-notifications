package uk.gov.nationalarchives.tre

import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, parser}
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Status}

object MessageParsingUtils {
  implicit val producerDecoder: Decoder[Producer.Value] = Decoder.decodeEnumeration(Producer)
  def parseGenericMessage(message: String): GenericMessage =
    parser.decode[GenericMessage](message).fold(error => throw new RuntimeException(error), identity)
}
case class GenericMessage(properties: Properties)