package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Concurrent
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import fs2.Stream
import io.circe.{Decoder, Encoder}
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services.*
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
    with DeleteOperations[F, Flavor]:
  
  override given createEncoder: Encoder[Flavor.Create] = Flavor.Create.given_Encoder_Create
  override given modelDecoder: Decoder[Flavor] = Flavor.given_Decoder_Flavor
  
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
    getByName(name).flatMap:
      case Some(model) => F.pure(model)
      case None => F.raiseError(new NoSuchElementException(s"""Could not find ${this.name} with name "$name"."""))
  
  /**
   * Lists summary information for all flavors.
   *
   * @param query extra query params to pass in the request.
   */
  def streamSummary(query: Query = Query.empty): Stream[F, Flavor.Summary] =
    super.stream(pluralName, uri.copy(query = query))
  
  /**
   * Lists summary information for all flavors.
   *
   * @param query extra query params to pass in the request.
   */
  def listSummary(query: Query = Query.empty): F[List[Flavor.Summary]] =
    super.list(pluralName, uri.copy(query = query))
  
  override def stream(query: Query, extraHeaders: Header.ToRaw*): Stream[F, Flavor] =
    stream(pluralName, (uri / "detail").copy(query = query), extraHeaders*)
  
  override def list(query: Query, extraHeaders: Header.ToRaw*): F[List[Flavor]] =
    list(pluralName, (uri / "detail").copy(query = query), extraHeaders*)
