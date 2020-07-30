package pt.tecnico.dsi.openstack.nova.models

import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object ServerSummary {
  implicit val decoder: Decoder[ServerSummary] = deriveDecoder
}
case class ServerSummary(
  id: String,
  name: Option[String] = None,
  links: List[Link] = List.empty,
) extends Identifiable
