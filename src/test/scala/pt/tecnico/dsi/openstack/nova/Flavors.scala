package pt.tecnico.dsi.openstack.nova

import cats.effect.{IO, Resource}
import pt.tecnico.dsi.openstack.nova.models.Flavor
import squants.information.InformationConversions.*

class Flavors extends Utils:
  import nova.flavors
  
  def createStub(name: String): Flavor.Create = Flavor.Create(
    id = Some(name),
    name = name,
    vcpus = 2,
    ram = 2.gibibytes,
    disk = 10.gibibytes,
  )
  
  val resource: Resource[IO, Flavor] =
    val create = withRandomName(name => flavors(createStub(name)))
    Resource.make(create)(model => flavors.delete(model.id))
  
  s"The flavor service" should:
    "list flavors" in resource.use { flavor =>
      flavors.list().idempotently(_ should contain (flavor))
    }
    
    "get flavors (existing id)" in resource.use { flavor =>
      flavors.get(flavor.id).idempotently(_.value shouldBe flavor)
    }
    "get flavors (non-existing id)" in:
      flavors.get("non-existing-id").idempotently(_ shouldBe None)
    
    "apply flavors (existing id)" in resource.use { flavor =>
      flavors.apply(flavor.id).idempotently(_ shouldBe flavor)
    }
    "apply flavors (non-existing id)" in:
      val id = "non-existing-id"
      flavors.apply(id).attempt.idempotently { either =>
        either.left.value shouldBe a[NoSuchElementException]
        val exception = either.left.value.asInstanceOf[NoSuchElementException]
        exception.getMessage should include (s"Could not find flavor")
      }
    
    "delete flavors" in resource.use { flavor =>
      flavors.delete(flavor.id).idempotently(_ shouldBe ())
    }
    
    "show flavors" in resource.use { flavor =>
      import cats.implicits.*
      //This line is a fail fast mechanism, and prevents false positives from the linter
      println(show"$flavor")
      IO("""show"$flavor"""" should compile)
    }
