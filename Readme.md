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
Please checkout the demo/readme!

### Ideas
* the pipelines should be visualized i.e. in [such a diagram](https://gojs.net/latest/samples/dynamicPorts.html)

