package pt.tecnico.dsi.openstack.nova.models

import java.time.{LocalDateTime, OffsetDateTime}
import cats.derived.derived
import cats.derived.ShowPretty
import io.circe.derivation.{ConfiguredCodec, ConfiguredEncoder}
import io.circe.Encoder
import org.typelevel.cats.time.instances.offsetdatetime.given
import org.typelevel.cats.time.instances.localdatetime.given
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.User

object Keypair:
  final case class Summary(
    name: String,
    publicKey: String,
    fingerprint: String,
  ) derives ConfiguredCodec, ShowPretty
  
  final case class Create(
    name: String,
    userId: Option[String] = None,
  ) derives ConfiguredEncoder, ShowPretty
final case class Keypair(
  name: String,
  publicKey: String,
  fingerprint: String,
  deleted: Boolean = false,
  userId: String,
  createdAt: LocalDateTime,
  updatedAt: Option[OffsetDateTime] = None,
  deletedAt: Option[OffsetDateTime] = None,
) extends Identifiable derives ConfiguredCodec, ShowPretty {
  // In the API documentation "Show Keypair Details" has a field id, however that field isn't used in all the other rest
  // api operations: read and delete its by name, list doesn't show id. By subverting the id to mean name we at least
  // get to use the DeleteOperations in the service class
  override def id: String = name
  override def links: List[Link] = List.empty
  
  def user[F[_]](using keystone: KeystoneClient[F]): F[User] = keystone.users(userId)
}