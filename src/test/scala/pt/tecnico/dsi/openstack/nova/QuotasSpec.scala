package pt.tecnico.dsi.openstack.nova

import cats.effect.IO
import org.http4s.client.UnexpectedStatus
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.nova.models.Quota
import squants.information.InformationConversions._

class QuotasSpec extends Utils {
  import nova.quotas

  "Quotas service" should {
    "get quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.get(project.id).idempotently(_.value.cores shouldBe 20)
    }
    "get quotas for a project (non-existing id)" in {
      quotas.get("non-existing-id").idempotently(_ shouldBe None)
    }
    "apply quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.apply(project.id).idempotently(_.cores shouldBe 20)
    }
    "apply quotas for a project (non-existing id)" in {
      quotas.apply("non-existing-id").attempt.idempotently(_.left.value shouldBe a [UnexpectedStatus])
    }

    "get usage quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.getUsage(project.id).idempotently(_.value.cores.limit shouldBe 20)
    }
    "get usage quotas for a project (non-existing id)" in {
      quotas.getUsage("non-existing-id").idempotently(_ shouldBe None)
    }
    "apply usage quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.applyUsage(project.id).idempotently(_.cores.limit shouldBe 20)
    }
    "apply usage quotas for a project (non-existing id)" in {
      quotas.applyUsage("non-existing-id").attempt.idempotently(_.left.value shouldBe a [UnexpectedStatus])
    }

    val defaultQuotas = Quota(
      cores = 20,
      instances = 10,
      keyPairs = 100,
      metadataItems = 128,
      ram = 50.gibibytes,
      serverGroups = 10,
      serverGroupMembers = 10,
      fixedIps = -1,
      floatingIps = 10,
      securityGroups = 10,
      securityGroupRules = 20,
      injectedFiles = 5,
      injectedFileContent = 10.kibibytes,
      injectedFilePath = 255.bytes,
    )

    "get default quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.getDefaults(project.id).idempotently(_.value shouldBe defaultQuotas)
    }
    "get default quotas for a project (non-existing id)" in {
      quotas.getDefaults("non-existing-id").idempotently(_ shouldBe None)
    }
    "apply default quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.applyDefaults(project.id).idempotently(_ shouldBe defaultQuotas)
    }
    "apply default quotas for a project (non-existing id)" in {
      quotas.applyDefaults("non-existing-id").attempt.idempotently(_.left.value shouldBe a [UnexpectedStatus])
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
