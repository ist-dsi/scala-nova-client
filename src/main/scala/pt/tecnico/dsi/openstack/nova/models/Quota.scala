package pt.tecnico.dsi.openstack.nova.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.derivation.{deriveDecoder, deriveEncoder, renaming}
import io.circe.{Decoder, Encoder}
import squants.information.Information

object Quota {
  object Create {
    implicit val encoder: Encoder.AsObject[Create] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  final case class Create(
    cores: Option[Int] = None,
    instances: Option[Int] = None,
    keyPairs: Option[Int] = None,
    metadataItems: Option[Int] = None,
    ram: Option[Information] = None,
    serverGroups: Option[Int] = None,
    serverGroupMembers: Option[Int] = None,
  )
  
  implicit val decoder: Decoder[Quota] = deriveDecoder(renaming.snakeCase)
  implicit val show: ShowPretty[Quota] = derived.semiauto.showPretty
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
final case class Quota(
  cores: Int,
  instances: Int,
  keyPairs: Int,
  metadataItems: Int,
  ram: Information,
  serverGroups: Int,
  serverGroupMembers: Int,
)