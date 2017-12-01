package kic.kafka.embedded

import java.util.Properties

import org.apache.kafka.clients.admin.AdminClient
import org.scalatest._

class EmbeddedKafaJavaWrapperTest extends FlatSpec with Matchers {
  val props = new Properties()

  "A running server" should "return a list of topics" in {
    props.setProperty("bootstrap.servers", "localhost:10001")
    EmbeddedKafaJavaWrapper.start(10001, 10002, props)

    try {
      val admin = AdminClient.create(props)
      val cluster = admin.describeCluster()
      val topicCount = admin.listTopics().names().get().size()
      topicCount should be >= 0
    } finally {
      EmbeddedKafaJavaWrapper.stop()
    }

  }

}
