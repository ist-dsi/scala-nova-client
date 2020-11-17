package pt.tecnico.dsi.openstack.nova

import cats.Show
import squants.information.Information

package object models {
  implicit val showInformation: Show[Information] = Show.fromToString
}
