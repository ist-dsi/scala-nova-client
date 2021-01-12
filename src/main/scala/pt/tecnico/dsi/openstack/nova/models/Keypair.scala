package pt.tecnico.dsi.openstack.nova.models

import java.time.{LocalDateTime, OffsetDateTime}
import cats.derived
import cats.derived.ShowPretty
import cats.effect.Sync
import io.circe.derivation.{deriveDecoder, deriveEncoder, renaming}
import io.circe.{Decoder, Encoder}
import io.chrisdavenport.cats.time.{localdatetimeInstances, offsetdatetimeInstances}
import pt.tecnico.dsi.openstack.common.models.{Identifiable, Link}
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.User

object Keypair {
  object Summary {
    implicit val decoder: Decoder[Summary] = deriveDecoder(renaming.snakeCase)
    implicit val show: ShowPretty[Summary] = derived.semiauto.showPretty
  }
  final case class Summary(
    name: String,
    publicKey: String,
    fingerprint: String,
  )
  
  object Create {
    implicit val encoder: Encoder.AsObject[Create] = deriveEncoder(renaming.snakeCase)
    implicit val show: ShowPretty[Create] = derived.semiauto.showPretty
  }
  final case class Create(
    name: String,
    userId: Option[String] = None,
  )
  
  implicit val decoder: Decoder[Keypair] = deriveDecoder(renaming.snakeCase)
  implicit val show: ShowPretty[Keypair] = derived.semiauto.showPretty
}
final case class Keypair(
  name: String,
  publicKey: String,
  fingerprint: String,
  deleted: Boolean = false,
  userId: String,
  createdAt: LocalDateTime,
  updatedAt: Option[OffsetDateTime] = None,
  deletedAt: Option[OffsetDateTime] = None,
) extends Identifiable {
  // In the API documentation "Show Keypair Details" has a field id, however that field isn't used in all the other rest
  // api operations: read and delete its by name, list doesn't show id. By subverting the id to mean name we at least
  // get to use the DeleteOperations in the service class
  override def id: String = name
  override def links: List[Link] = List.empty
  
  def user[F[_]: Sync](implicit keystone: KeystoneClient[F]): F[User] = keystone.users(userId)
}