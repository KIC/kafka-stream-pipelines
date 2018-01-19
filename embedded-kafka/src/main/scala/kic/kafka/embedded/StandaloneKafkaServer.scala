package kic.kafka.embedded

import java.io.{File, FileReader}
import java.util.Properties

object StandaloneKafkaServer extends App {

  val properties = new Properties()

  try {
    properties.load(ClassLoader.getSystemResourceAsStream("embeddedkafka.properties"))
    if (args.length > 2) properties.load(new FileReader(new File(args(3)).getAbsoluteFile))

    println("starting embedded kafka ... ")
    EmbeddedKafaJavaWrapper.start(
      if (args.length > 0) args(1).toInt else 9092,
      if (args.length > 1) args(2).toInt else 2181,
      properties
    )
  } catch {
    case e: Throwable => println(s"start with args: kafka-port, zookeeper-port, kafka-properties-file\n$e")
  }

}
