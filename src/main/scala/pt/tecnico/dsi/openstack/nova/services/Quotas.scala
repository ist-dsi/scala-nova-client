package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Concurrent
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.http4s.Uri
import org.http4s.client.{Client, UnexpectedStatus}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.{Quota, QuotaUsage}

final class Quotas[F[_]: Concurrent: Client](baseUri: Uri, session: Session) extends Service[F](baseUri, "quota_set", session.authToken):
  override val uri: Uri = baseUri / "os-quota-sets"
  
  private val wrappedAt: Option[String] = Some(name)

  private def buildUri(projectId: String, userId: Option[String]): Uri = (uri / projectId).withOptionQueryParam("user_id", userId)

  // In a Rest API if the resource does not exist you return a BadRequest </sarcasm>
  private def getOption[R: Decoder](uri: Uri): F[Option[R]] =
    import cats.syntax.functor.*
    import dsl.*
    import org.http4s.EntityDecoder
    import org.http4s.Method.GET
    import org.http4s.Status.{BadRequest, Successful}

    given EntityDecoder[F, R] = unwrapped(Some(name))
    val request = GET(uri, authToken)
    client.run(request).use:
      case Successful(response) => response.as[R].map(Option.apply)
      case BadRequest(_) => F.pure(Option.empty[R])
      case response => F.raiseError(UnexpectedStatus(response.status, request.method, request.uri))

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
    super.get(wrappedAt, buildUri(projectId, userId))

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
    super.get(wrappedAt, buildUri(projectId, userId) / "detail")

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
    super.get(wrappedAt, uri / projectId / "defaults")

  /**
   * Update the quotas for a project or a project and a user.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   * @param force whether to force the update even if the quota has already been used and the reserved quota exceeds the new quota.
   */
  def update(projectId: String, quotas: Quota.Update, userId: Option[String] = None, force: Boolean = false): F[Quota] =
    given Encoder[Quota.Update] = Quota.Update.derived$ConfiguredEncoder.mapJsonObject(_.add("force", force.asJson))
    super.put(wrappedAt, quotas, buildUri(projectId, userId))

  /**
   * Reverts the quotas to default values for a project or a project and a user.
   *
   * @param projectId UUID of the project.
   * @param userId id of user to list the quotas for.
   */
  def delete(projectId: String, userId: Option[String] = None): F[Unit] = super.delete(buildUri(projectId, userId))
