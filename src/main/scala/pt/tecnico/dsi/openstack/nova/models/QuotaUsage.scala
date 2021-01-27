package pt.tecnico.dsi.openstack.nova.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.Codec
import io.circe.derivation.{deriveCodec, renaming}
import pt.tecnico.dsi.openstack.common.models.Usage
import squants.information.Information

object QuotaUsage {
  implicit val codec: Codec[QuotaUsage] = deriveCodec(renaming.snakeCase)
  implicit val show: ShowPretty[QuotaUsage] = derived.semiauto.showPretty
}
/**
  * A value of -1 means no limit.
 * @param cores number of allowed server cores for each project.
 * @param instances number of allowed servers for each project.
 * @param keyPairs number of allowed key pairs for each user.
 * @param metadataItems number of allowed metadata items for each server.
 * @param ram amount of allowed server RAM, in MiB, for each project.
 * @param serverGroups number of server groups that are allowed for each project.
 * @param serverGroupMembers number of allowed members for each server group.
  */
case class QuotaUsage(
  instances: Usage[Int],
  cores: Usage[Int],
  ram: Usage[Information],
  keyPairs: Usage[Int],
  metadataItems: Usage[Int],
  serverGroups: Usage[Int],
  serverGroupMembers: Usage[Int],
)
