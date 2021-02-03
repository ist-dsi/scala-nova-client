package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Concurrent
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Stream
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services._
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.Flavor

// Flavors seem to be un-updatable. We are always receiving a "resource not found" no matter what

/**
 * @define domainModel Flavor
 */
final class Flavors[F[_]: Concurrent: Client](baseUri: Uri, session: Session)
  extends PartialCrudService[F](baseUri, "flavor", session.authToken)
    with CreateNonIdempotentOperations[F, Flavor, Flavor.Create]
    with ListOperations[F, Flavor]
    with ReadOperations[F, Flavor]
    with DeleteOperations[F, Flavor] {
  
  override implicit val createEncoder: Encoder[Flavor.Create] = Flavor.Create.encoder
  override implicit val modelDecoder: Decoder[Flavor] = Flavor.decoder
  
  /**
   * Gets the $domainModel with the specified `name`.
   * @param name the name of the $domainModel to get.
   * @note Nova does not allow listing/searching flavors by name. So this method lists all flavors and finds the one named `name`.
   * @return a Some with the $domainModel if it exists. If more than one exists the first one. A None otherwise.
   */
  def getByName(name: String): F[Option[Flavor]] = list().map(_.find(_.name == name))
  
  /**
   * Gets the $domainModel with the specified `name`, assuming it exists.
   * @param name the name of the $domainModel to get.
   * @note Nova does not allow listing/searching flavors by name. So this method lists all flavors and finds the one named `name`.
   * @return the $domainModel with the given `name`. If more than one exists the first one. If none exists F will contain an error.
   */
  def applyByName(name: String): F[Flavor] =
    getByName(name).flatMap {
      case Some(model) => F.pure(model)
      case None => F.raiseError(new NoSuchElementException(s"""Could not find ${this.name} with name "$name"."""))
    }
  
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
