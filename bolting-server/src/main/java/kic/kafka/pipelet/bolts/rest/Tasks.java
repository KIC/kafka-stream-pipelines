package kic.kafka.pipelet.bolts.rest;

import kic.kafka.pipelet.bolts.dto.TaskDescription;
import kic.kafka.pipelet.bolts.services.lambda.BoltingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/v1/tasks")
public class Tasks {

    @Autowired
    private BoltingService boltingService;

    @RequestMapping(path = "", method = RequestMethod.GET, produces = "application/json")
    private List<TaskDescription> getTasks() {
        return boltingService.getTasks()
                             .stream()
                             .map(TaskDescription::new)
                             .collect(toList());
    }

}
