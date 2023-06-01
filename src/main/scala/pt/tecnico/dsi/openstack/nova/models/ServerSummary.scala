package pt.tecnico.dsi.openstack.nova.models

import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.ConfiguredCodec
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

case class ServerSummary(
  id: String,
  name: String,
  links: List[Link] = List.empty,
) extends Identifiable derives ConfiguredCodec, ShowPretty
