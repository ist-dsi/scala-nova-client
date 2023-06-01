package pt.tecnico.dsi.openstack.nova.models

import cats.Show
import io.circe.derivation.Configuration
import io.circe.{Codec, Decoder, Encoder}
import squants.information.Information
import squants.information.InformationConversions.*

given Configuration = Configuration.default.withDefaults.withSnakeCaseMemberNames

// Sizes/Quotas in Nova are always in mebibytes.
// When setting sizes/quotas they are always whole numbers.
given Codec[Information] = Codec.from(Decoder.decodeInt.map(_.mebibytes), Encoder.encodeInt.contramap(_.toMebibytes.ceil.toInt))

given Show[Information] = Show.fromToString
