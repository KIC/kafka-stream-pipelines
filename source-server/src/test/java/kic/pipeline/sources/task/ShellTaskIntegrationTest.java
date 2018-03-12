package kic.pipeline.sources.task;

import kic.pipeline.sources.spring.entities.JobState;
import kic.pipeline.sources.spring.entities.KeyValueLog;
import kic.pipeline.sources.spring.repository.JobLogRepository;
import kic.pipeline.sources.spring.repository.JobRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;

@DataJpaTest
@SpringBootTest
@ActiveProfiles("unittest")
@RunWith(SpringJUnit4ClassRunner.class)
public class ShellTaskIntegrationTest {

    @Autowired
    private JobRepository jobStateService;

    @Autowired
    private JobLogRepository jobLogService;

    @Test(expected = org.springframework.dao.DataIntegrityViolationException.class)
    public void testDuplicateKey() {
        KeyValueLog log1 = new KeyValueLog("a", "b", "c");
        KeyValueLog log2 = new KeyValueLog("a", "b", "c");
        jobLogService.save(log1);
        jobLogService.save(log2);
    }

    @Test
    public void testExecuteShellTask() {
        ShellTask shelltask = createShelltask();
        shelltask.execute(null);
        JobState result = jobStateService.findOne("job-123");
        System.out.println(result);
    }

    private ShellTask createShelltask() {
        return new ShellTask("job-123",
                             "UTF-8",
                             "",
                             new File(".").getAbsoluteFile(),
                             Arrays.asList("java", "-cp", "${CLASS_PATH}", "cli.Echo", "Hello World:22\nlala:44"),
                             Arrays.asList("java", "-cp", "${CLASS_PATH}", "cli.Split", ":", "0"),
                             Arrays.asList("java", "-cp", "${CLASS_PATH}", "cli.Split", ":", "1"),
                             (k, v) -> {},
                             jobStateService::findOrNew,
                             jobStateService::save);
    }

}