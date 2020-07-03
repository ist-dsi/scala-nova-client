package pt.tecnico.dsi.openstack.nova.models

import cats.arrow.FunctionK
import cats.{Id, ~>}
import io.circe.syntax._
import io.circe.{Codec, Decoder, Encoder, HCursor, JsonObject}
import squants.information.{Bytes, Information, InformationUnit, Mebibytes}

object Quota {
  def informationCodecIn(unit: InformationUnit): Codec[Information] =
    Codec.from(Decoder.decodeInt.map(unit(_)), Encoder.encodeInt.contramap(_.to(unit).ceil.toInt))

  val inMebibytesCodec: Codec[Information] = informationCodecIn(Mebibytes)
  val inBytesCodec: Codec[Information] = informationCodecIn(Bytes)

  // Its better to have this slightly uglier than to repeat it for the QuotaUsage.
  private[models] def decoder[F[_], T](f: (F[Int], F[Int], F[Int], F[Int], F[Information], F[Int], F[Int],
    F[Int], F[Int], F[Int], F[Int], F[Int], F[Information], F[Information]) => T)
    (lift: Decoder ~> λ[A => Decoder[F[A]]]): Decoder[T] = (cursor: HCursor) => {
    // Polymorphic function types would be handy https://blog.oyanglul.us/scala/dotty/en/rank-n-type
    implicit def convert[A](implicit decoder: Decoder[A]): Decoder[F[A]] = lift(decoder)
    for {
      cores <- cursor.get[F[Int]]("cores")
      instances <- cursor.get[F[Int]]("instances")
      keyPairs <- cursor.get[F[Int]]("key_pairs")
      metadataItems <- cursor.get[F[Int]]("metadata_items")
      ram <- cursor.get[F[Information]]("ram")(lift(inMebibytesCodec))
      serverGroups <- cursor.get[F[Int]]("server_groups")
      serverGroupMembers <- cursor.get[F[Int]]("server_group_members")
      fixedIps <- cursor.get[F[Int]]("fixed_ips")
      floatingIps <- cursor.get[F[Int]]("floating_ips")
      securityGroups <- cursor.get[F[Int]]("security_groups")
      securityGroupRules <- cursor.get[F[Int]]("security_group_rules")
      injectedFiles <- cursor.get[F[Int]]("injected_files")
      injectedFileContentBytes <- cursor.get[F[Information]]("injected_file_content_bytes")(lift(inBytesCodec))
      injectedFilePathBytes <- cursor.get[F[Information]]("injected_file_path_bytes")(lift(inBytesCodec))
    } yield f(cores, instances, keyPairs, metadataItems, ram, serverGroups, serverGroupMembers, fixedIps, floatingIps,
      securityGroups, securityGroupRules, injectedFiles, injectedFileContentBytes, injectedFilePathBytes)
  }
  implicit val decoder: Decoder[Quota] = decoder[Id, Quota](Quota.apply)(FunctionK.id)

  object Create {
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
}
// We could just have:
//   QuotaBase[F[_]](cores: F[Int], ...)
//   type Quota = QuotaBase[Id]
//   type QuotaUsage = QuotaBase[Usage]

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