package kic.kafka.embedded

import java.io.{File, FileReader}
import java.util.Properties

object StandaloneKafkaServer {

  def main(args: Array[String]): Unit = {
    val properties = new Properties()

    try {
      properties.load(new FileReader(new File(args(3)).getAbsoluteFile))
      EmbeddedKafaJavaWrapper.start(args(1).toInt, args(2).toInt, properties)
    } catch {
      case _ => println("start with args: kafka-port, zookeeper-port, kafka-properties-file")
    }
  }

}
