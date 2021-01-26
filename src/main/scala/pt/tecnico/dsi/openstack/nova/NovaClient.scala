package pt.tecnico.dsi.openstack.nova

import cats.effect.Concurrent
import org.http4s.Uri
import org.http4s.client.Client
import pt.tecnico.dsi.openstack.keystone.models.{ClientBuilder, Session}
import pt.tecnico.dsi.openstack.nova.services._

object NovaClient extends ClientBuilder {
	final type OpenstackClient[F[_]] = NovaClient[F]
	final val `type`: String = "compute"
	
	override def apply[F[_]: Concurrent: Client](baseUri: Uri, session: Session): NovaClient[F] =
		new NovaClient[F](baseUri, session)
}
class NovaClient[F[_]: Concurrent](baseUri: Uri, session: Session)(implicit client: Client[F]) {
	val uri: Uri = if (baseUri.path.dropEndsWithSlash.toString.endsWith("v2.1")) baseUri else baseUri / "v2.1"

	val quotas = new Quotas[F](uri, session)
	val servers = new Servers[F](uri, session)
	val keypairs = new Keypairs[F](uri, session)
	val usageReports = new UsageReports[F](uri, session)
}
