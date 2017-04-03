package integration

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Args, Status, TestSuiteMixin}
import org.scalatestplus.play.{PlaySpec, ServerProvider}
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneServerPerSuite}
import play.api.Application
import play.api.libs.ws.WSClient
import play.api.test.{Helpers, TestServer}

class IntegrationSpec extends PlaySpec
  with TestSuiteMixin
  with ServerProvider
  with GuiceFakeApplicationFactory
  with ScalaFutures
  with IntegrationPatience {

  implicit lazy val app: Application = fakeApplication()

  lazy val sslPort = 19443

  lazy val port: Int = Helpers.testServerPort

  // Override the base server per suite to run explicitly on SSL port
  override def run(testName: Option[String], args: Args): Status = {
    val testServer = TestServer(port, app, Option(sslPort))
    testServer.start()
    try {
      val newConfigMap = args.configMap +
        ("org.scalatestplus.play.app" -> app) +
        ("org.scalatestplus.play.port" -> port) +
        ("org.scalatestplus.play.sslPort" -> sslPort)
      val newArgs = args.copy(configMap = newConfigMap)
      val status = super.run(testName, newArgs)
      status.whenCompleted { _ => testServer.stop() }
      status
    }
    catch { // In case the suite aborts, ensure the server is stopped
      case ex: Throwable =>
        testServer.stop()
        throw ex
    }
  }

  "WSClient" should {

    "connect to the local server over HTTPS" in {
      val client = app.injector.instanceOf[WSClient]
      val responseFuture = client.url(s"https://localhost:${sslPort}/").get()

      whenReady(responseFuture) { response =>
        val jsonOutput = response.json
        val tlsVersion = (jsonOutput \ "tls_version").as[String]
        tlsVersion must contain("TLS 1.2")
      }
    }
  }

}
