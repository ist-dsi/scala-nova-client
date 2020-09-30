package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Sync
import cats.syntax.flatMap._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.Uri
import org.http4s.client.Client
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.{Quota, QuotaUsage}

final class Quotas[F[_]: Sync: Client](baseUri: Uri, session: Session) extends Service[F](session.authToken) {
  val uri: Uri = baseUri / "os-quota-sets"
  val name = "quota_set"

  private def buildUri(projectId: String, userId: Option[String]): Uri = (uri / projectId).withOptionQueryParam("user_id", userId)

  // In a Rest API if the resource does not exist you return a BadRequest not a NotFound </sarcasm>
  private def getOption[R: Decoder](uri: Uri): F[Option[R]] = {
    import cats.syntax.applicative._
    import cats.syntax.functor._
    import dsl._
    import org.http4s.EntityDecoder
    import org.http4s.Method.GET
    import org.http4s.Status.{BadRequest, Successful}

    implicit val d: EntityDecoder[F, R] = unwrapped(Some(name))
    GET(uri, authToken).flatMap(client.run(_).use {
      case Successful(response) => response.as[R].map(Option.apply)
      case BadRequest(_) => Option.empty[R].pure[F]
    })
  }

  /**
   * Shows quotas for a project.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def get(projectId: String, userId: Option[String] = None): F[Option[Quota]] =
    getOption(buildUri(projectId, userId))
  /**
   * Shows quotas for a project assuming the project/user exist.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def apply(projectId: String, userId: Option[String] = None): F[Quota] =
    super.get(wrappedAt = Some(name), buildUri(projectId, userId))

  /**
   * Shows quota usage for a project.
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def getUsage(projectId: String, userId: Option[String] = None): F[Option[QuotaUsage]] =
    getOption(buildUri(projectId, userId) / "detail")
  /**
   * Shows quota usage for a project assuming the project/user exist.
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def applyUsage(projectId: String, userId: Option[String] = None): F[QuotaUsage] =
    super.get(wrappedAt = Some(name), buildUri(projectId, userId) / "detail")

  /**
   * Gets default quotas for a project.
   * @param projectId UUID of the project.
   */
  def getDefaults(projectId: String): F[Option[Quota]] =
    getOption(uri / projectId / "defaults")
  /**
   * Gets default quotas for a project assuming the project exists.
   * @param projectId UUID of the project.
   */
  def applyDefaults(projectId: String): F[Quota] =
    super.get(wrappedAt = Some(name), uri / projectId / "defaults")

  /**
   * Update the quotas for a project or a project and a user.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   * @param force whether to force the update even if the quota has already been used and the reserved quota exceeds the new quota.
   */
  def update(projectId: String, quotas: Quota.Create, userId: Option[String] = None, force: Boolean = false): F[Quota] = {
    val forcedEncoder: Encoder[Quota.Create] = implicitly[Encoder.AsObject[Quota.Create]].mapJsonObject(_.add("force", force.asJson))
    super.put(wrappedAt = Some(name), quotas, buildUri(projectId, userId))(forcedEncoder, Quota.decoder)
  }

  /**
   * Reverts the quotas to default values for a project or a project and a user.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def delete(projectId: String, userId: Option[String] = None): F[Unit] = super.delete(buildUri(projectId, userId))
}
