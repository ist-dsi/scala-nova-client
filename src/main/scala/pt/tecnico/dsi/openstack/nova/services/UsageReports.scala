package pt.tecnico.dsi.openstack.nova.services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import cats.effect.Sync
import org.http4s.{QueryParamEncoder, Uri}
import org.http4s.client.Client
import pt.tecnico.dsi.openstack.common.services.PartialCrudService
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.UsageReport

final class UsageReports[F[_]: Sync: Client](baseUri: Uri, session: Session) extends PartialCrudService[F](baseUri, "tenant_usage", session.authToken) {
  override val uri: Uri = baseUri / "os-simple-tenant-usage"
  
  implicit val localDateTimeQueryParamEncoder: QueryParamEncoder[LocalDateTime] =
    QueryParamEncoder[String].contramap[LocalDateTime] { (dateTime: LocalDateTime) =>
      // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
      formatter.format(dateTime)
    }
  
  /**
   * Shows the usage report for a project.
   *
   * @param projectId UUID of the project.
   */
  def get(projectId: String, start: Option[LocalDateTime], end: Option[LocalDateTime]): F[Option[UsageReport]] =
    getOption(wrappedAt, (uri / projectId).withOptionQueryParam("start", start).withOptionQueryParam("end", end))
  /**
   * Shows the usage report for a project assuming the project exist.
   *
   * @param projectId UUID of the project.
   */
  def apply(projectId: String, start: Option[LocalDateTime], end: Option[LocalDateTime]): F[UsageReport] =
    super.get(wrappedAt, (uri /  projectId).withOptionQueryParam("start", start).withOptionQueryParam("end", end))
}