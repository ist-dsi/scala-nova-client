package pt.tecnico.dsi.openstack.nova.models

import io.circe.Decoder
import io.circe.derivation.deriveDecoder

object ServerSummary {
  implicit val decoder: Decoder[ServerSummary] = deriveDecoder
}
case class ServerSummary(name: Option[String] = None)
