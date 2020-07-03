package pt.tecnico.dsi.openstack.nova

import cats.effect.IO
import pt.tecnico.dsi.openstack.keystone.models.Project
import pt.tecnico.dsi.openstack.nova.models.Quota
import squants.information.InformationConversions._

class QuotasSpec extends Utils {
  val withStubProject: IO[(NovaClient[IO], String)] =
    for {
      keystone <- keystoneClient
      dummyProject <- keystone.projects.create(Project("dummy", "dummy project", "default"))
      nova <- client
    } yield (nova, dummyProject.id)

  "Quotas service" should {
    "get default quotas for a project" in withStubProject.flatMap { case (nova, dummyProjectId) =>
      nova.quotas.getDefaults(dummyProjectId).idempotently { quotas =>
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
    "get quotas for a project" in withStubProject.flatMap { case (nova, dummyProjectId) =>
      nova.quotas.get(dummyProjectId).idempotently(_.cores shouldBe 20)
    }
    "get usage quotas for a project" in withStubProject.flatMap { case (nova, dummyProjectId) =>
      nova.quotas.getUsage(dummyProjectId).idempotently(_.cores.limit shouldBe 20)
    }
    "update quotas for a project" in withStubProject.flatMap { case (nova, dummyProjectId) =>
      val newQuotas = Quota.Create(cores = Some(25), instances = Some(25), ram = Some(20.gibibytes))
      nova.quotas.update(dummyProjectId, newQuotas).idempotently { quota =>
        quota.cores shouldBe 25
        quota.ram shouldBe 20.gibibytes
        quota.instances shouldBe 25
      }
    }
    "delete quotas for a project" in withStubProject.flatMap { case (nova, dummyProjectId) =>
      nova.quotas.delete(dummyProjectId).idempotently(_ shouldBe ())
    }
  }
}
