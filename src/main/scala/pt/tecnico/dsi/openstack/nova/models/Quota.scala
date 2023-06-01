package pt.tecnico.dsi.openstack.nova.models

import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.{ConfiguredCodec, ConfiguredEncoder}
import io.circe.{Codec, Encoder}
import squants.information.Information
import squants.information.InformationConversions.*

object Quota:
  final case class Create(
    instances: Option[Int] = None,
    cores: Option[Int] = None,
    ram: Option[Information] = None,
    keyPairs: Option[Int] = None,
    metadataItems: Option[Int] = None,
    serverGroups: Option[Int] = None,
    serverGroupMembers: Option[Int] = None,
  ) derives ConfiguredEncoder, ShowPretty
  
  object Update:
    val zero: Update = Update(
      instances = Some(0),
      cores = Some(0),
      ram = Some(0.bytes),
      keyPairs = Some(0),
      metadataItems = Some(0),
      serverGroups = Some(0),
      serverGroupMembers = Some(0),
    )
  final case class Update(
    instances: Option[Int] = None,
    cores: Option[Int] = None,
    ram: Option[Information] = None,
    keyPairs: Option[Int] = None,
    metadataItems: Option[Int] = None,
    serverGroups: Option[Int] = None,
    serverGroupMembers: Option[Int] = None,
  ) derives ConfiguredEncoder, ShowPretty:
    lazy val needsUpdate: Boolean =
      // We could implement this with the next line, but that implementation is less reliable if the fields of this class change
      //  productIterator.asInstanceOf[Iterator[Option[Any]]].exists(_.isDefined)
      List(instances, cores, ram, keyPairs, metadataItems, serverGroups, serverGroupMembers).exists(_.isDefined)
  
  val zero: Quota = Quota(0, 0, 0.gibibytes, 0, 0, 0, 0)
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
  instances: Int,
  cores: Int,
  ram: Information,
  keyPairs: Int,
  metadataItems: Int,
  serverGroups: Int,
  serverGroupMembers: Int,
) derives ConfiguredCodec, ShowPretty