package pt.tecnico.dsi.openstack.nova

import scala.annotation.nowarn
import cats.effect.IO
import cats.syntax.show._
import pt.tecnico.dsi.openstack.common.models.UnexpectedStatus
import org.scalatest.Assertion
import pt.tecnico.dsi.openstack.common.models.Usage
import pt.tecnico.dsi.openstack.nova.models.{Quota, QuotaUsage}
import squants.information.InformationConversions._

class QuotasSpec extends Utils {
  import nova.quotas

  val defaultQuotas = Quota(
    cores = 20,
    instances = 10,
    keyPairs = 100,
    metadataItems = 128,
    ram = 50.gibibytes,
    serverGroups = 10,
    serverGroupMembers = 10,
  )

  val defaultQuotaUsage = QuotaUsage(
    cores = Usage(0, defaultQuotas.cores, 0),
    instances = Usage(0, defaultQuotas.instances, 0),
    keyPairs = Usage(0, defaultQuotas.keyPairs, 0),
    metadataItems = Usage(0, defaultQuotas.metadataItems, 0),
    ram = Usage(0.gibibytes, defaultQuotas.ram, 0.gibibytes),
    serverGroups = Usage(0, defaultQuotas.serverGroups, 0),
    serverGroupMembers = Usage(0, defaultQuotas.serverGroupMembers, 0),
  )

  "Quotas service" should {
    "get quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.get(project.id).idempotently(_.value shouldBe defaultQuotas)
    }
    "get quotas for a project (non-existing id)" in {
      quotas.get("non-existing-id").idempotently(_ shouldBe None)
    }
    "apply quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.apply(project.id).idempotently(_ shouldBe defaultQuotas)
    }
    "apply quotas for a project (non-existing id)" in {
      quotas.apply("non-existing-id").attempt.idempotently(_.left.value shouldBe a [UnexpectedStatus])
    }

    "get usage quotas for a project (existing id)" in withStubProject.use[IO, Assertion] { project =>
      quotas.getUsage(project.id).idempotently(_.value shouldBe defaultQuotaUsage)
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
      val newQuotas = Quota.Update(cores = Some(25), instances = Some(25), ram = Some(20.gibibytes))
      quotas.update(project.id, newQuotas).idempotently { quota =>
        quota.cores shouldBe 25
        quota.ram shouldBe 20.gibibytes
        quota.instances shouldBe 25
      }
    }
    "delete quotas for a project" in withStubProject.use[IO, Assertion] { project =>
      quotas.delete(project.id).idempotently(_ shouldBe ())
    }
    
    s"show quotas" in withStubProject.use[IO, Assertion] { project =>
      quotas.apply(project.id).map { quotas =>
        //This line is a fail fast mechanism, and prevents false positives from the linter
        println(show"$quotas")
        """show"$quotas"""" should compile: @nowarn
      }
    }
    s"show quota usage" in withStubProject.use[IO, Assertion] { project =>
      quotas.getUsage(project.id).map { quotaUsage =>
        //This line is a fail fast mechanism, and prevents false positives from the linter
        println(show"$quotaUsage")
        """show"$quotaUsage"""" should compile: @nowarn
      }
    }
  }
}
