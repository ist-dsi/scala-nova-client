package pt.tecnico.dsi.openstack.nova.models

import io.circe.{Decoder, HCursor}
import pt.tecnico.dsi.openstack.common.models.Usage
import pt.tecnico.dsi.openstack.nova.models.Quota.{inBytesCodec, inMebibytesCodec}
import squants.information.Information

object QuotaUsage {
  implicit val decoder: Decoder[QuotaUsage] = (cursor: HCursor) => for {
    cores <- cursor.get[Usage[Int]]("cores")
    instances <- cursor.get[Usage[Int]]("instances")
    keyPairs <- cursor.get[Usage[Int]]("key_pairs")
    metadataItems <- cursor.get[Usage[Int]]("metadata_items")
    ram <- cursor.get[Usage[Information]]("ram")(Usage.decoder(inMebibytesCodec))
    serverGroups <- cursor.get[Usage[Int]]("server_groups")
    serverGroupMembers <- cursor.get[Usage[Int]]("server_group_members")
    fixedIps <- cursor.get[Usage[Int]]("fixed_ips")
    floatingIps <- cursor.get[Usage[Int]]("floating_ips")
    securityGroups <- cursor.get[Usage[Int]]("security_groups")
    securityGroupRules <- cursor.get[Usage[Int]]("security_group_rules")
    injectedFiles <- cursor.get[Usage[Int]]("injected_files")
    injectedFileContentBytes <- cursor.get[Usage[Information]]("injected_file_content_bytes")(Usage.decoder(inBytesCodec))
    injectedFilePathBytes <- cursor.get[Usage[Information]]("injected_file_path_bytes")(Usage.decoder(inBytesCodec))
  } yield QuotaUsage(cores, instances, keyPairs, metadataItems, ram, serverGroups, serverGroupMembers, fixedIps, floatingIps,
    securityGroups, securityGroupRules, injectedFiles, injectedFileContentBytes, injectedFilePathBytes)
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
case class QuotaUsage(
  cores: Usage[Int],
  instances: Usage[Int],
  keyPairs: Usage[Int],
  metadataItems: Usage[Int],
  ram: Usage[Information],
  serverGroups: Usage[Int],
  serverGroupMembers: Usage[Int],
  fixedIps: Usage[Int],
  floatingIps: Usage[Int],
  securityGroups: Usage[Int],
  securityGroupRules: Usage[Int],
  injectedFiles: Usage[Int],
  injectedFileContent: Usage[Information],
  injectedFilePath: Usage[Information],
)
