package pt.tecnico.dsi.openstack.nova

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Random
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.instances.list.*
import cats.syntax.traverse.*
import org.http4s.client.Client
import org.http4s.blaze.client.BlazeClientBuilder
import org.log4s.*
import org.scalatest.*
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import pt.tecnico.dsi.openstack.keystone.KeystoneClient
import pt.tecnico.dsi.openstack.keystone.models.Project

abstract class Utils extends AsyncWordSpec with Matchers with BeforeAndAfterAll with OptionValues with EitherValues:
  val logger: Logger = getLogger
  
  val (_httpClient, finalizer) = BlazeClientBuilder[IO]
    .withResponseHeaderTimeout(20.seconds)
    .withCheckEndpointAuthentication(false)
    .resource.allocated.unsafeRunSync()
  
  override protected def afterAll(): Unit = finalizer.unsafeRunSync()
  
  /*import org.http4s.Headers
  import org.http4s.client.middleware.{Logger, ResponseLogger}
  import org.typelevel.ci.CIString
  given Client[IO] = Logger.colored(
    logHeaders = true,
    logBody = true,
    redactHeadersWhen = (Headers.SensitiveHeaders ++ List(CIString("X-Auth-Token"), CIString("X-Subject-Token"))).contains,
    responseColor = ResponseLogger.defaultResponseColor[IO] _
  )(_httpClient)*/
  given Client[IO] = _httpClient
  
  val keystone: KeystoneClient[IO] = KeystoneClient.authenticateFromEnvironment().unsafeRunSync()
  val nova: NovaClient[IO] = keystone.session.clientBuilder[IO](NovaClient, sys.env("OS_REGION_NAME"))
    .fold(s => throw new Exception(s), identity)
  
  // Not very purely functional :(
  val random = new Random()
  def randomName(): String = random.alphanumeric.take(10).mkString.dropWhile(_.isDigit).toLowerCase
  def withRandomName[T](f: String => IO[T]): IO[T] = IO.delay(randomName()).flatMap(f)
  
  val withStubProject: Resource[IO, Project] =
    val create = withRandomName(name => keystone.projects.create(Project.Create(name)))
    Resource.make(create)(project => keystone.projects.delete(project))

  extension [T](io: IO[T])
    def idempotently(test: T => Assertion, repetitions: Int = 3): IO[Assertion] =
      require(repetitions >= 2, "To test for idempotency at least 2 repetitions must be made")
      io.flatMap { firstResult =>
        // If this fails we do not want to mask its exception with "Operation is not idempotent".
        // Because failing in the first attempt means whatever is being tested in `test` is not implemented correctly.
        test(firstResult)
        (2 to repetitions).toList.traverse { _ =>
          io
        } map { results =>
          // And now we want to catch the exception because if `test` fails here it means it is not idempotent.
          try
            results.foreach(test)
            succeed
          catch
            case e: TestFailedException =>
              val numberOfDigits = Math.floor(Math.log10(repetitions.toDouble)).toInt + 1
              val resultsString = (firstResult +: results).zipWithIndex
                .map { case (result, i) =>
                  s" %${numberOfDigits}d: %s".format(i + 1, result)
                }.mkString("\n")
              throw e.modifyMessage(_.map(message =>
                s"""Operation is not idempotent. Results:
                   |$resultsString
                   |$message""".stripMargin))
        }
      }

  given Conversion[IO[Assertion], Future[Assertion]] = _.unsafeToFuture()
