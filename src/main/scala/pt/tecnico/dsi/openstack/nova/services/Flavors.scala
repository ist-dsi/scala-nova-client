package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Concurrent
import fs2.Stream
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services._
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.Flavor

// Flavors seem to be un-updatable. We are always receiving a "resource not found" no matter what

final class Flavors[F[_]: Concurrent: Client](baseUri: Uri, session: Session)
  extends PartialCrudService[F](baseUri, "flavor", session.authToken)
    with CreateNonIdempotentOperations[F, Flavor, Flavor.Create]
    with ListOperations[F, Flavor]
    with ReadOperations[F, Flavor]
    with DeleteOperations[F, Flavor] {
  
  override implicit val createEncoder: Encoder[Flavor.Create] = Flavor.Create.encoder
  override implicit val modelDecoder: Decoder[Flavor] = Flavor.decoder
  
  /**
   * Lists summary information for all flavors.
   *
   * @param query extra query params to pass in the request.
   */
  def streamSummary(query: Query = Query.empty): Stream[F, Flavor.Summary] =
    super.stream[Flavor.Summary](pluralName, uri.copy(query = query))
  
  /**
   * Lists summary information for all flavors.
   *
   * @param query extra query params to pass in the request.
   */
  def listSummary(query: Query = Query.empty): F[List[Flavor.Summary]] =
    super.list[Flavor.Summary](pluralName, uri.copy(query = query))
  
  override def stream(query: Query, extraHeaders: Header*): Stream[F, Flavor] =
    stream[Flavor](pluralName, (uri / "detail").copy(query = query), extraHeaders:_*)
  
  override def list(query: Query, extraHeaders: Header*): F[List[Flavor]] =
    list[Flavor](pluralName, (uri / "detail").copy(query = query), extraHeaders:_*)
}
