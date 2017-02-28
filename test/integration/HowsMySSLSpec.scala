package integration

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.junit.runner._
import org.specs2.runner._
import play.api.Mode
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.ahc.{AhcWSClient, AhcWSClientConfigFactory}
import play.api.test._

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class HowsMySSLSpec extends PlaySpecification {

  "WS" should {

    "connect to a remote server " in {

      val name = "testing"
      val system = ActorSystem(name)
      implicit val materializer = ActorMaterializer(namePrefix = Some(name))(system)

      val input = """play.ws.ssl {
                    |  //enabledProtocols = [ TLSv1.2 ]
                    |}
                  """.stripMargin
      val config = ConfigFactory.parseString(input).withFallback(ConfigFactory.defaultReference())
      val wsConfig = AhcWSClientConfigFactory.forConfig(config)
      val client = AhcWSClient(wsConfig)
      val response = await(client.url("https://www.howsmyssl.com/a/check").get())(2 seconds)
      val jsonOutput = response.json

      system.terminate()
      client.close()

      val tlsVersion = (jsonOutput \ "tls_version").as[String]
      tlsVersion must contain("TLS 1.2")
    }
  }

}
