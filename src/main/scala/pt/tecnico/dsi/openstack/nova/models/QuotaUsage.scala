package pt.tecnico.dsi.openstack.nova.models

import cats.~>
import io.circe.Decoder
import pt.tecnico.dsi.openstack.common.models.Usage
import squants.information.Information

object QuotaUsage {
  val usageDecoderFunctionK: Decoder ~> λ[A => Decoder[Usage[A]]] = new (Decoder ~> λ[A => Decoder[Usage[A]]]) {
    override def apply[A](fa: Decoder[A]): Decoder[Usage[A]] = Usage.decoder(fa)
  }
  implicit val decoder: Decoder[QuotaUsage] = Quota.decoder[Usage, QuotaUsage](QuotaUsage.apply)(usageDecoderFunctionK)
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
