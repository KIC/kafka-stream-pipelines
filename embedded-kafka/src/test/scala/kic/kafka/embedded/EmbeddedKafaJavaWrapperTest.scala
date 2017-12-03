package kic.kafka.embedded

import java.util.Properties

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.scalatest._

import scala.collection.JavaConverters._

class EmbeddedKafaJavaWrapperTest extends FlatSpec with Matchers {
  val props = new Properties()

  "A running server" should "return a list of topics" in {
    props.setProperty("bootstrap.servers", "localhost:10001")
    props.setProperty("delete.enable.topic", "true")
    EmbeddedKafaJavaWrapper.start(10001, 10002, props)

    try {
      val admin = AdminClient.create(props)
      admin.createTopics(Seq(new NewTopic("test-topic", 1, 1)).asJava)

      val topics = admin.listTopics().names().get()
      info(s"topics: $topics")
      topics should contain("test-topic")

      // we should be able to send something to this topic
      // delete this topic
      // recreate this topic
      // consume this topic and expect to get 0 entries

    } finally {
      EmbeddedKafaJavaWrapper.stop()
    }

  }

}
