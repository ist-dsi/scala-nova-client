package pt.tecnico.dsi.openstack.nova.models

import cats.derived
import cats.derived.ShowPretty
import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder, HCursor, JsonObject}
import squants.information.{Bytes, Information, InformationUnit, Mebibytes}
import pt.tecnico.dsi.openstack.nova.models.showInformation

object Quota {
  def informationCodecIn(unit: InformationUnit): Codec[Information] =
    Codec.from(Decoder.decodeInt.map(unit(_)), Encoder.encodeInt.contramap(_.to(unit).ceil.toInt))
  
  val inMebibytesCodec: Codec[Information] = informationCodecIn(Mebibytes)
  val inBytesCodec: Codec[Information] = informationCodecIn(Bytes)
  
  object Create {
    // Yup that's whats consistency looks like, some things are in MiB others in Bytes </sarcasm>
    implicit val encoder: Encoder.AsObject[Create] = (quota: Create) => JsonObject(
      "cores" -> quota.cores.asJson,
      "instances" -> quota.instances.asJson,
      "key_pairs" -> quota.keyPairs.asJson,
      "metadata_items" -> quota.metadataItems.asJson,
      "ram" -> quota.ram.asJson(Encoder.encodeOption(inMebibytesCodec)),
      "server_groups" -> quota.serverGroups.asJson,
      "server_group_members" -> quota.serverGroupMembers.asJson,
      "fixed_ips" -> quota.fixedIps.asJson,
      "floating_ips" -> quota.floatingIps.asJson,
      "security_groups" -> quota.securityGroups.asJson,
      "security_group_rules" -> quota.securityGroupRules.asJson,
      "injected_files" -> quota.injectedFiles.asJson,
      "injected_file_content" -> quota.injectedFileContent.asJson(Encoder.encodeOption(inBytesCodec)),
      "injected_file_path" -> quota.injectedFilePath.asJson(Encoder.encodeOption(inBytesCodec)),
    )
    
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
    fixedIps: Option[Int] = None,
    floatingIps: Option[Int] = None,
    securityGroups: Option[Int] = None,
    securityGroupRules: Option[Int] = None,
    injectedFiles: Option[Int] = None,
    injectedFileContent: Option[Information] = None,
    injectedFilePath: Option[Information] = None,
  )
  
  // Yup that's whats consistency looks like, some things are in MiB others in Bytes </sarcasm>
  implicit val decoder: Decoder[Quota] = (cursor: HCursor) => for {
    cores <- cursor.get[Int]("cores")
    instances <- cursor.get[Int]("instances")
    keyPairs <- cursor.get[Int]("key_pairs")
    metadataItems <- cursor.get[Int]("metadata_items")
    ram <- cursor.get[Information]("ram")(inMebibytesCodec)
    serverGroups <- cursor.get[Int]("server_groups")
    serverGroupMembers <- cursor.get[Int]("server_group_members")
    fixedIps <- cursor.get[Int]("fixed_ips")
    floatingIps <- cursor.get[Int]("floating_ips")
    securityGroups <- cursor.get[Int]("security_groups")
    securityGroupRules <- cursor.get[Int]("security_group_rules")
    injectedFiles <- cursor.get[Int]("injected_files")
    injectedFileContentBytes <- cursor.get[Information]("injected_file_content_bytes")(inBytesCodec)
    injectedFilePathBytes <- cursor.get[Information]("injected_file_path_bytes")(inBytesCodec)
  } yield Quota(cores, instances, keyPairs, metadataItems, ram, serverGroups, serverGroupMembers, fixedIps, floatingIps,
    securityGroups, securityGroupRules, injectedFiles, injectedFileContentBytes, injectedFilePathBytes)
  
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
 * @param fixedIps number of allowed fixed IP addresses for each project. Must be equal to or greater than the number of allowed servers.
 * @param floatingIps number of allowed floating IP addresses for each project.
 * @param securityGroups number of allowed security groups for each project.
 * @param securityGroupRules number of allowed rules for each security group.
 * @param injectedFiles number of allowed injected files for each project.
 * @param injectedFileContent number of allowed bytes of content for each injected file.
 * @param injectedFilePath number of allowed bytes for each injected file path.
 */
final case class Quota(
  cores: Int,
  instances: Int,
  keyPairs: Int,
  metadataItems: Int,
  ram: Information,
  serverGroups: Int,
  serverGroupMembers: Int,
  fixedIps: Int,
  floatingIps: Int,
  securityGroups: Int,
  securityGroupRules: Int,
  injectedFiles: Int,
  injectedFileContent: Information,
  injectedFilePath: Information,
)