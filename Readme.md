## Kafka Streaming Pipeline
The idea ist to enable stream procesing while all the transformations are applied, 
changed or removed at runtime. And every trnsformation is just a REST like service.
This way one can develop and maintian the not only the whole data pipeline but also
every transition logic with 0 downtime.
    
### Bolting Pipelets
Basically we have a Kafa cluser where we subscribe to a specific topic, then
push forward the event to a rest api and finally sent the rest result to a target 
topic. such kind of stateless functions are also called serverless functions or 
thanks to amazon _lambdas_.  

Now imaginge a sequence of such labdas - this is what we call a pipeline.
A lamda + the definition of the pipeline and the topics is what we call a 
pipelet. Now to attach one pipelet to another we bolt them together.

|===||===||===||===||===||===||===||===||===||===||===||===||===|

### Demo
* compile demo `./gradlew clean :demo:build` 
* run the demo `java -jar demo/build/libs/demo-1.0-SNAPSHOT.jar`
    * this starts an embedded kafka cluster 
    * and the bolting server
* browse to [http://localhost:8080/](http://localhost:8080/) 
* add "demo.returs" topic as a "bar" chart
* open up a new console and run `./gradlew :demo:startSource`
* you should see the returns chart updating on the ui
* add the topic "demo.performance" as a "line" chart
* run curl and bolt demo service to source
```
curl -H "Content-Type: text/plain" \
-X POST 'http://localhost:8080/api/v1/bolt/demo-pipeline/demo-service-1/demo.returns/demo.performance/GET' \
-d 'http://localhost:4567/demo/fold?key=${event.key}&value=${event.value}&state=${state}&offset=${state.nextConsumerOffset()}'
```
* run `./gradlew :demo:startService` in yet another new terminal window
* the performance chart should update in sync with the returns chart

now we can do some stuff like 
* kill and restart demo service without dataloss
* _persist the pipeline and also allow to restart the bolting service (comming up next)_
* _replace service by a different one (comming up next)_

### Development
* For the react frontend we have a webpack development server: `./gradlew :demo:frontend:start --no-daemon`
* We have embedded a kafa server into the demo module. During development boot up time with
the full embedded kafka is too slow so we can start a kafka server during development like so: 
`./gradlew :embedded-kafka:start`   
* A demo source can be started with: `./gradlew :demo:startSource`
* A demo service can be started with: `./gradlew :demo:startService` 

### TODO
- [ ] persist pipelets and state and test server restart. 
- [ ] sources: just like pipelets we also want to discover sources 
available for the bolting machine. All sources should also be backupped
by some database so that we can replay whole pipelines if neccessary 
- [ ] versioning of topics: if we repplace a pipelet we want to re-run
all the topics depending on the replaced target topic. To do so we need 
to have some kind of versioned topics.


### Ideas
* the pipelines should be visualized i.e. in [such a diagram](https://gojs.net/latest/samples/dynamicPorts.html)

### next steps
make a new module for sources, introduce a cron syntax like cron4j and use shell commands
as sources where we simple read the stout and a key extractor as well as a value extractor
we pipe the stdout to the std in of the extractors. the result we just push to our kafka topic.
this way one could use simple curl commands and/or javascript as sources

make an selfcontained command line pipeline like one could do on unix 
`curl "" | awk "x"` using https://docs.oracle.com/javase/7/docs/api/java/io/PipedInputStream.html

use process builder:
```  
ProcessBuilder pb = new ProcessBuilder();
pb.redirectInput(new FileInputStream(new File(infile));
pb.redirectOutput(new FileOutputStream(new File(outfile));
pb.command(cmd);
pb.start().waitFor();
``` 

we stick all the commands in a file like so:
cron       | command                                                                 | key-extractor | value-extractor
*/1 * * *  | curl "http://../..?key=$lastKey&value=$lastValue&lastResut=$lastResult" | grep key      | grap value 

Note: we need to filter the last known line as we need to avoid duplicates