package pt.tecnico.dsi.openstack.nova.services

import cats.effect.Sync
import cats.syntax.functor._
import fs2.Stream
import io.circe.{Decoder, HCursor}
import org.http4s.client.Client
import org.http4s.{Header, Query, Uri}
import pt.tecnico.dsi.openstack.common.services._
import pt.tecnico.dsi.openstack.keystone.models.Session
import pt.tecnico.dsi.openstack.nova.models.Keypair

// This service/model is easily one of the most inconsistent/poorly programmed API/models of Openstack

final class Keypairs[F[_]: Sync: Client](baseUri: Uri, session: Session)
  extends PartialCrudService[F](baseUri, "keypair", session.authToken)
    with ReadOperations[F, Keypair]
    with DeleteOperations[F, Keypair] {
  // Cannot extend CreateNonIdempotentOperations because create returns (String, Keypair.Summary) and not Keypair
  //  and extending CreateNonIdempotentOperations[F, (String, Keypair.Summary), Keypair.Create] causes
  //  a collision on modelDecoder :(
  // Cannot extend CreateOperations because we cannot extend CreateNonIdempotentOperations, and also createOrUpdate
  // must return F[(Option[String], Keypair.Summary)] whereas create returns F[(String, Keypair.Summary)].
  
  override val uri: Uri = baseUri / "os-keypairs"
  
  override implicit val modelDecoder: Decoder[Keypair] = Keypair.decoder
  
  /**
   * Streams summary information for keypairs.
   *
   * @param query extra query params to pass in the request.
   */
  def streamSummary(query: Query = Query.empty): Stream[F, Keypair.Summary] = {
    // Double wrapping for the extra gift sensation
    implicit val summaryDecoder: Decoder[Keypair.Summary] = Keypair.Summary.decoder.at(name)
    super.stream[Keypair.Summary](pluralName, uri.copy(query = query))
  }
  
  /**
   * Lists summary information for keypairs.
   *
   * @param query extra query params to pass in the request.
   */
  def listSummary(query: Query = Query.empty): F[List[Keypair.Summary]] =
    streamSummary(query).compile.toList
  
  /**
   * Imports an existing keypair into openstack.
   * @param name the name to give to the keypair in Openstack.
   * @param publicKey the public key to import.
   */
  def importPublicKey(name: String, publicKey: String): F[Keypair.Summary] = {
    postHandleConflict(wrappedAt, Map("name" -> name, "public_key" -> publicKey), uri, Seq.empty) {
      apply(name).map { key =>
        // TODO: this is not correct. If the publicKeys do not match we should raiseError
        Keypair.Summary(key.name, key.publicKey, key.fingerprint)
      }
    }
  }
  
  /**
   * Creates a new keypair with the given `create` values.
   * @param create the values to use in the create.
   * @param extraHeaders extra headers to pass when making the request. The `authToken` header is always added.
   * @return the private key along side with the keypair summary.
   */
  def create(create: Keypair.Create, extraHeaders: Header*): F[(String, Keypair.Summary)] = {
    implicit val decoder: Decoder[(String, Keypair.Summary)] = (cursor: HCursor) => for {
      privateKey <- cursor.get[String]("private_key")
      keypair <- cursor.as[Keypair.Summary]
    } yield (privateKey, keypair)
    post(wrappedAt, create, uri, extraHeaders:_*)
  }
  
  def apply(create: Keypair.Create, extraHeaders: Header*): F[(String, Keypair.Summary)] =
    this.create(create, extraHeaders:_*)
  
  def createOrUpdate(create: Keypair.Create, extraHeaders: Header*): F[(Option[String], Keypair.Summary)] = {
    implicit val decoder: Decoder[(Option[String], Keypair.Summary)] = (cursor: HCursor) => for {
      privateKey <- cursor.get[Option[String]]("private_key")
      keypair <- cursor.as[Keypair.Summary]
    } yield (privateKey, keypair)
    postHandleConflict[Keypair.Create, (Option[String], Keypair.Summary)](wrappedAt, create, uri, extraHeaders) {
      apply(create.name).map { key =>
        (Option.empty, Keypair.Summary(key.name, key.publicKey, key.fingerprint))
      }
    }
  }
  
  // These methods were overriden to change the first parameter name to `name` instead of `id`.
  // Why? Because Openstack likes to be consistent </sarcasm>.
  override def get(name: String, extraHeaders: Header*): F[Option[Keypair]] = super.get(name, extraHeaders:_*)
  override def apply(name: String, extraHeaders: Header*): F[Keypair] = super.apply(name, extraHeaders:_*)
  override def delete(name: String, extraHeaders: Header*): F[Unit] = super.delete(name, extraHeaders:_*)
}
