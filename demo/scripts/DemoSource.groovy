@Grab(group='commons-cli', module='commons-cli', version='1.4')
import org.apache.commons.cli.*

@Grab(group='org.apache.commons', module='commons-math3', version='3.6.1')
import org.apache.commons.math3.distribution.NormalDistribution

@Grab(group='org.apache.kafka', module='kafka-clients', version='1.0.0')
import org.apache.kafka.clients.admin.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.errors.TopicExistsException

Options commandlineOptions = new Options().with {
    addOption "i", "interval", true, "interval of emitting random numbers (default 5000ms)"
    addOption "n", "numberOfNumbers", true, "max number of random numbers generated (default Integer.MAX_VALUE)"
    addOption "t", "topic", true, "target topic (default 'demo.returns')"
    addOption "s", "bootstrapServers", false, "kafka bootstrap servers (default 'localhost:9092')"
    addOption "h", "help", false, "display this help"
}

def switches = new DefaultParser().parse(commandlineOptions, args)
if (switches.hasOption("h")) {
    new HelpFormatter().printHelp( new File(getClass().protectionDomain.codeSource.location.path).name, commandlineOptions)
    System.exit(-1)
}

def kafkaProps = [
    "bootstrap.servers": switches.getOptionValue("s", "localhost:9092"),
    "key.serializer": "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer": "org.apache.kafka.common.serialization.StringSerializer",
    "acks": "all"
]

def topic = switches.getOptionValue("t", "demo.returns")
def producer = new KafkaProducer(kafkaProps);
def dist = new NormalDistribution(0, 0.02)
def maxNumbers = switches.getOptionValue("n", "${Integer.MAX_VALUE}").toInteger()
def count = 0

println("create topic: $topic")
try {
    AdminClient.create(kafkaProps).createTopics([new NewTopic(topic, 1, (short) 1)]).all().get()
} catch (TopicExistsException) {}

while(maxNumbers > count) {
    def value = dist.sample()
    def key = System.currentTimeMillis().toString()
    def offset = producer.send(new ProducerRecord(topic, key, value.toString())).get().offset()
    producer.flush()
    println("sent $key\t$value to: $topic with offset: $offset / $maxNumbers")
    Thread.sleep(switches.getOptionValue("i", "5000").toLong())
    count++
}
