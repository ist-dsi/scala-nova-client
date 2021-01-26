package pt.tecnico.dsi.openstack.nova

import cats.effect.{IO, Resource}
import pt.tecnico.dsi.openstack.nova.models.Keypair

class KeypairsSpec extends Utils {
  import nova.keypairs
  
  val withStubKeypair: Resource[IO, (String, Keypair.Summary)] = {
    val create = withRandomName(name => keypairs.create(Keypair.Create(name)))
    Resource.make(create)(model => keypairs.delete(model._2.name))
  }
  
  "Keypairs service" should {
    "list keypair summary" in withStubKeypair.use { case (_, summary) =>
      keypairs.listSummary().idempotently { models =>
        models.exists(_.name == summary.name) shouldBe true
      }
    }
    
    "createOrUpdate keypairs" in {
      val keypairCreate = Keypair.Create(randomName())
      for {
        result <- keypairs.createOrUpdate(keypairCreate).idempotently { case (privateKey, summary) =>
          // Java does not come with out-of-the-box support for PKCS1 keys:
          // https://stackoverflow.com/questions/41934846/read-rsa-private-key-of-format-pkcs1-in-java
          // We didn't want to deal with BouncyCastle so the test is not very strong
          if (privateKey.isDefined) {
            privateKey.value should startWith("-----BEGIN RSA PRIVATE KEY-----")
          }
          summary.name shouldBe keypairCreate.name
          summary.publicKey should startWith("ssh-rsa")
          summary.fingerprint.split(":").length shouldBe 16
        }
        // If the createOrUpdate isn't idempotent this won't clean up all created keypairs
        _ <- keypairs.delete(keypairCreate.name)
      } yield result
    }
    
    "import keypairs" in {
      // The keypair was generated with the following command, because to implement the same in Java would require
      // BouncyCastle (or something like it).
      // ssh-keygen -t rsa -b 1024 -f test.key; cat test.key.pub
      val name = randomName()
      val publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDr3m9p7p5oDhVw/3TriypL4bs48bMyQkU0mYibBS12iaRAELtCIUW7104QFqg6dTx4YCJIak/4YbWA3bfc7pYLsfBbpS3qrWGW8ctXfbewMs4GQ4CWN23O/ryRQvSLLcSL1T3kq6tC0a3oI4+3mpinxRctccZ9f5UdZORP4bII3Q=="
      keypairs.importPublicKey(name, publicKey).idempotently { summary =>
        summary.name shouldBe name
        summary.publicKey should startWith("ssh-rsa")
        summary.fingerprint.split(":").length shouldBe 16
      }
    }
    
    "get keypairs (existing name)" in withStubKeypair.use { case (_, summary) =>
      keypairs.get(summary.name).idempotently { keypair =>
        keypair.value.name shouldBe summary.name
        keypair.value.fingerprint shouldBe summary.fingerprint
        keypair.value.publicKey shouldBe summary.publicKey
        keypair.value.deleted shouldBe false
      }
    }
    "get keypairs (non-existing name)" in {
      keypairs.get("non-existing-name").idempotently(_ shouldBe None)
    }
    
    "apply keypairs (existing name)" in withStubKeypair.use { case (_, summary) =>
      keypairs.apply(summary.name).idempotently {
        keypair =>
          keypair.name shouldBe summary.name
          keypair.fingerprint shouldBe summary.fingerprint
          keypair.publicKey shouldBe summary.publicKey
          keypair.deleted shouldBe false
      }
    }
    "apply quotas (non-existing name)" in {
      keypairs.apply("non-existing-id").attempt.idempotently(_.left.value shouldBe a [NoSuchElementException])
    }
    
    "delete keypairs" in withStubKeypair.use { case (_, summary) =>
      keypairs.delete(summary.name).idempotently(_ shouldBe ())
    }
  }
}
