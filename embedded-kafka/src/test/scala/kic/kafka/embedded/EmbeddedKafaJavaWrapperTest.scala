package kic.kafka.embedded

import java.util.{Properties, UUID}

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.scalatest._

import scala.collection.JavaConverters._

class EmbeddedKafaJavaWrapperTest extends FlatSpec with Matchers {
  val props = new Properties()
  val testTopic = "test-topic"

  "A running server" should "return a list of topics" in {
    props.setProperty("bootstrap.servers", "localhost:10001")
    props.setProperty("delete.enable.topic", "true")
    props.setProperty("group.id", "test-client")
    props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer")
    props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.setProperty("clinet.id", "test-client")
    props.setProperty("key.serializer", "org.apache.kafka.common.serialization.LongSerializer")
    props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    EmbeddedKafaJavaWrapper.start(10001, 10002, props)

    try {
      implicit val admin = AdminClient.create(props)

      // create topic and confirm it exists
      createTopic(testTopic)
      listTopics() should contain(testTopic)

      // now we should be able to send something to this topic
      // FIXME NOT YET WORKING -> topic can not be deleted afterwards
      // val producer = new KafkaProducer[Long, String](props)
      //producer.send(new ProducerRecord[Long, String](testTopic, System.currentTimeMillis(), "test")).get()
      //producer.close()

      // delete topic
      deleteTopic(testTopic)
      listTopics() shouldNot contain(testTopic)

      // recreate topic
      createTopic(testTopic)
      // listTopics() should contain(testTopic) // FIXME NOT YET WORKING: https://issues.apache.org/jira/browse/KAFKA-6302

      // and finally consume from the topic and expect to get 0 entries
      // TODO create consumer and poll once
      val consumer = new KafkaConsumer[Long, String](props)
      consumer.subscribe(Seq(testTopic).asJava)
      consumer.poll(0)
      consumer.seekToBeginning(Seq(new TopicPartition(testTopic, 0)).asJava)
      consumer.poll(1000).count() should be(0)
    } finally {
      EmbeddedKafaJavaWrapper.stop()
    }

  }

  def listTopics()(implicit admin: AdminClient) = {
    val topicList = admin.listTopics().names().get()
    info(s"topics: $topicList")
    topicList
  }

  def createTopic(topic: String)(implicit admin: AdminClient) =
    admin.createTopics(Seq(new NewTopic(topic, 1, 1)).asJava)

  def deleteTopic(topic: String)(implicit admin: AdminClient) =
    admin.deleteTopics(Seq("test-topic").asJava).all().get()

}
