package pt.tecnico.dsi.openstack.nova

import cats.Show
import io.circe.{Codec, Decoder, Encoder}
import squants.information.Information
import squants.information.InformationConversions._

package object models {
  // Sizes/Quotas in Nova are always in mebibytes.
  // When setting sizes/quotas they are always whole numbers.
  implicit val codecInformation: Codec[Information] = Codec.from(Decoder.decodeInt.map(_.mebibytes), Encoder.encodeInt.contramap(_.toMebibytes.ceil.toInt))
  
  implicit val showInformation: Show[Information] = Show.fromToString
}
