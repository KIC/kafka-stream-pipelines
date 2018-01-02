## To Demo
* compile demo `./gradlew clean :demo:build` 
* run the demo `java -jar demo/build/libs/demo-1.0-SNAPSHOT.jar`
* when everything started 
    * open browser at http://localhost:8080/
    * go to /scripts
* run `groovy DemoSource.groovy`
* check ui, should update graph
* run `groovy DemoService.groovy`
* run curl and bolt demo service to source
```
curl -H "Content-Type: text/plain" \
-X POST 'http://localhost:8080/api/v1/bolt/demo-pipeline/demo-service-1/demo.returns/demo.performance/GET' \
-d 'http://localhost:4567/demo/fold?key=${event.key}&value=${event.value}&state=${state}'
```
* check ui chart should update

now we can do some checks like 
* kill and restart demo service without dataloss
* replace service by a different one

## Development
./gradlew :demo:frontend:start --no-daemon

#### OLD


To run the demo you need to open up two terminal windows. 
* Go to the first one and run <br>
  `java -jar /build/libs/demo-1.0-SNAPSHOT.jar`
* After the emedded kafka has been stared, go to the second one and run <br>
  `python src/main/python3/demo-dash.py`

Then navigate your browser to [http://localhost:8050/](http://localhost:8050/). 
You should see a continously updating graph like this.

![chart](snip_20171228175559.png)

What happens here is that we generate some random numbers into a kafka topic. 
There is one piplet which reads from that topic and creates a cumulative timeseries.
We simply read both topics and plot them.  
