package pt.tecnico.dsi.openstack.nova.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}

object ServerSummary {
  implicit val codec: Codec[ServerSummary] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[ServerSummary] = derived.semiauto.showPretty
}
case class ServerSummary(
  id: String,
  name: String,
  links: List[Link] = List.empty,
) extends Identifiable
