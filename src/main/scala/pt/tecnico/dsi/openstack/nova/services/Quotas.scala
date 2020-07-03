package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Sync
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.common.models.WithId
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.nova.models.{Quota, QuotaUsage}

final class Quotas[F[_]: Sync: Client](baseUri: Uri, authToken: Header) extends Service[F](authToken) {
  val uri: Uri = baseUri / "os-quota-sets"
  val name = "quota_set"

  private def buildUri(projectId: String, userId: Option[String]): Uri = (uri / projectId).withOptionQueryParam("user_id", userId)

  /**
   * Shows quotas for a project.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def get(projectId: String, userId: Option[String] = None): F[WithId[Quota]] =
    super.get(buildUri(projectId, userId), wrappedAt = Some(name))

  /**
   * Shows quota usage for a project.
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def getUsage(projectId: String, userId: Option[String] = None): F[WithId[QuotaUsage]] =
    super.get(buildUri(projectId, userId) / "detail", wrappedAt = Some(name))

  /**
   * Gets default quotas for a project.
   * @param projectId UUID of the project.
   */
  def getDefaults(projectId: String): F[WithId[Quota]] = super.get(uri / projectId / "defaults", wrappedAt = Some(name))

  /**
   * Update the quotas for a project or a project and a user.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   * @param force whether to force the update even if the quota has already been used and the reserved quota exceeds the new quota.
   */
  def update(projectId: String, quotas: Quota.Create, userId: Option[String] = None, force: Boolean = false): F[Quota] = {
    val forcedEncoder: Encoder[Quota.Create] = implicitly[Encoder.AsObject[Quota.Create]].mapJsonObject(_.add("force", force.asJson))
    super.put(quotas, buildUri(projectId, userId), wrappedAt = Some(name))(forcedEncoder, Quota.decoder)
  }

  /**
   * Reverts the quotas to default values for a project or a project and a user.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def delete(projectId: String, userId: Option[String] = None): F[Unit] = super.delete(buildUri(projectId, userId))
}
