import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
class SampleUnitTest {

    //@ClassRule
    //public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true);

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void someTest() {
        println(embeddedKafka.kafkaServers.size())
    }
}