package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Concurrent
import fs2.Stream
import org.http4s.client.Client
import org.http4s.{Query, Uri}
import pt.tecnico.dsi.openstack.common.services.Service
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.ServerSummary

final class Servers[F[_]: Concurrent: Client](baseUri: Uri, session: Session) extends Service[F](baseUri, "server", session.authToken):
  /**
   * Lists summary information for all servers the project ID associated with the authenticated request can access.
   *
   * @param query extra query params to pass in the request.
   */
  def streamSummary(query: Query = Query.empty): Stream[F, ServerSummary] =
    super.stream[ServerSummary](pluralName, uri.copy(query = query))
  
  /**
   * Lists summary information for all servers the project ID associated with the authenticated request can access.
   *
   * @param query extra query params to pass in the request.
   */
  def listSummary(query: Query = Query.empty): F[List[ServerSummary]] =
    super.list[ServerSummary](pluralName, uri.copy(query = query))
  
  /**
   * Deletes a server.
   *
   * @param serverId UUID of the server.
   */
  def delete(serverId: String): F[Unit] = super.delete(uri / serverId)
