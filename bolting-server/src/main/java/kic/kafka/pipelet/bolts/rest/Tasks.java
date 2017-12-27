package kic.kafka.pipelet.bolts.rest;

import kic.kafka.pipelet.bolts.dto.TaskDescription;
import kic.kafka.pipelet.bolts.services.lambda.BoltingService;
import kic.kafka.pipelet.bolts.services.lambda.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/tasks")
public class Tasks {

    @Autowired
    private BoltingService boltingService;

    @RequestMapping(path = "/", method = RequestMethod.POST)
    private List<TaskDescription> getTasks() {
        return getDescriptionFromStream(
                Stream.concat(
                    boltingService.getActiveTasks().stream(),
                    boltingService.getFailingTasks().stream()));
    }

    @RequestMapping(path = "/active", method = RequestMethod.POST)
    private List<TaskDescription> getActiveTasks() {
        return getDescriptionFromStream(boltingService.getActiveTasks().stream());
    }

    @RequestMapping(path = "/failing", method = RequestMethod.POST)
    private List<TaskDescription> getFailingTasks() {
        return getDescriptionFromStream(boltingService.getFailingTasks().stream());
    }

    private List<TaskDescription> getDescriptionFromStream(Stream<Task> stream) {
        return stream.map(TaskDescription::new)
                     .collect(toList());
    }

}
