package pt.tecnico.dsi.openstack.nova

import cats.effect.Sync
import org.http4s.client.Client
import org.http4s.{Header, Uri}
import pt.tecnico.dsi.openstack.nova.services.{Quotas, Servers}

class NovaClient[F[_]: Sync](baseUri: Uri, authToken: Header)(implicit client: Client[F]) {
	val uri: Uri = if (baseUri.path.dropEndsWithSlash.toString.endsWith("v2.1")) baseUri else baseUri / "v2.1"

	val quotas = new Quotas[F](uri, authToken)
	val servers = new Servers[F](uri, authToken)
}
