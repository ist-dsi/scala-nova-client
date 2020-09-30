package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Sync
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Query, Uri}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.ServerSummary

final class Servers[F[_]: Sync: Client](baseUri: Uri, session: Session) extends Service[F](session.authToken) {
  val name = "server"
  val pluralName = s"${name}s"
  val uri: Uri = baseUri / pluralName

  /**
   * Lists summary information for all servers the project ID associated with the authenticated request can access.
   *
   * @param query extra query params to pass in the request.
   */
  def listSummary(query: Query = Query.empty): Stream[F, ServerSummary] =
    super.list[ServerSummary](pluralName, uri, query)

  /**
   * Deletes a server.
   *
   * @param serverId UUID of the server.
   */
  def delete(serverId: String): F[Unit] = super.delete(uri / serverId)
}
