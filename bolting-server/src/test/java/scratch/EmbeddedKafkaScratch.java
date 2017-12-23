package scratch;

import net.manub.embeddedkafka.EmbeddedKafka$;
import net.manub.embeddedkafka.EmbeddedKafkaConfig;
import net.manub.embeddedkafka.EmbeddedKafkaConfig$;
import scala.collection.immutable.HashMap;

public class EmbeddedKafkaScratch {

    public static void main(String[] args) throws Exception {
        EmbeddedKafkaConfig config = EmbeddedKafkaConfig$.MODULE$.apply(1000, 1001, new HashMap<>(), new HashMap<>(), new HashMap<>());

        System.out.println("starting kafka");
        EmbeddedKafka$.MODULE$.start(config);

        System.out.println("hanging around");
        Thread.sleep(10000);

        // FIXME try to get kafka admin client and list topics AdminClient.create(configuration.getKafka());
        /*
        adminClient.listTopics()
                   .listings()
                   .get(1000L, TimeUnit.MILLISECONDS)
                   .stream()
                   .map(TopicListing::name)
                   .forEach(sout);
        */

        System.out.println("stoping kafka");
        EmbeddedKafka$.MODULE$.stop();
    }
}
