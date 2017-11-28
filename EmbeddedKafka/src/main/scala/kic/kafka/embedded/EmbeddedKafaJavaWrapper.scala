package kic.kafka.embedded

import java.util.Properties

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import scala.collection.JavaConverters._

object EmbeddedKafaJavaWrapper {

  def start(kafkaPort: Int, zookeeperPort: Int, properties: Properties): Unit = {
    val scalaProperties = properties.asScala.toMap
    EmbeddedKafka.start()(EmbeddedKafkaConfig.apply(kafkaPort, zookeeperPort, scalaProperties, scalaProperties, scalaProperties))
  }

  def stop(): Unit =
    EmbeddedKafka.stop()

}