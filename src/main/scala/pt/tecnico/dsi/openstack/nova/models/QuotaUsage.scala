package pt.tecnico.dsi.openstack.nova.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.Decoder
import io.circe.derivation.{deriveDecoder, renaming}
import pt.tecnico.dsi.openstack.common.models.Usage
import squants.information.Information

object QuotaUsage {
  implicit val decoder: Decoder[QuotaUsage] = deriveDecoder(renaming.snakeCase)
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
  cores: Usage[Int],
  instances: Usage[Int],
  keyPairs: Usage[Int],
  metadataItems: Usage[Int],
  ram: Usage[Information],
  serverGroups: Usage[Int],
  serverGroupMembers: Usage[Int],
)
