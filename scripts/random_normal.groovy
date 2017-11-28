
@Grab(group='org.apache.commons', module='commons-math3', version='3.6.1')
import org.apache.commons.math3.distribution.NormalDistribution

@Grab(group='org.apache.kafka', module='kafka-clients', version='1.0.0')
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.KafkaAdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

def props = new Properties()
props.put("bootstrap.servers", "localhost:9092");
props.put("acks", "all");
props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

def adminClient = (KafkaAdminClient) AdminClient.create(props);
def producer = new KafkaProducer<Long, String>(props);
def dist = new NormalDistribution(0, 0.02)
def topic = "test111"

def newTopics = adminClient.createTopics([new NewTopic(topic, 1, (short) 1)])
println(newTopics)

while(true) {
    def sample = dist.sample()
    println(sample)
    producer.send(new ProducerRecord<Long, String>(topic, System.currentTimeMillis(), "" + sample))
    Thread.sleep(args.length > 0 ? args[0].toLong() : 100L)
}