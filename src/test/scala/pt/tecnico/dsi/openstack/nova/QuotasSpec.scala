package pt.tecnico.dsi.openstack.nova

import cats.effect.IO
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.nova.models.Quota
import squants.information.InformationConversions._

class QuotasSpec extends Utils {
  import nova.quotas

  "Quotas service" should {
    "get default quotas for a project" in withStubProject.use[IO, Assertion] { project =>
      quotas.getDefaults(project.id).idempotently { quotas =>
        quotas.cores shouldBe 20
        quotas.ram shouldBe 50.gibibytes
        quotas.keyPairs shouldBe 100
        quotas.serverGroups shouldBe 10
        quotas.serverGroupMembers shouldBe 10
        quotas.floatingIps shouldBe 10
        quotas.instances shouldBe 10
        quotas.securityGroups shouldBe 10
        quotas.securityGroupRules shouldBe 20
      }
    }
    "get quotas for a project" in withStubProject.use[IO, Assertion] { project =>
      quotas.get(project.id).idempotently(_.cores shouldBe 20)
    }
    "get usage quotas for a project" in withStubProject.use[IO, Assertion] { project =>
      quotas.getUsage(project.id).idempotently(_.cores.limit shouldBe 20)
    }
    "update quotas for a project" in withStubProject.use[IO, Assertion] { project =>
      val newQuotas = Quota.Create(cores = Some(25), instances = Some(25), ram = Some(20.gibibytes))
      quotas.update(project.id, newQuotas).idempotently { quota =>
        quota.cores shouldBe 25
        quota.ram shouldBe 20.gibibytes
        quota.instances shouldBe 25
      }
    }
    "delete quotas for a project" in withStubProject.use[IO, Assertion] { project =>
      quotas.delete(project.id).idempotently(_ shouldBe ())
    }
  }
}
