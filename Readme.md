## Bolting Pipelets
Basically we have a Kafa cluser wehre we can substribe to a specific topic
pushForward the event to a rest api and sent the rest result to a target topic.
such kind of stateless functions are also called serverless functions or 
thanks to amazon _labdas_.  

Now imaginge a sequence of such labdas - this is what we call a pipeline.
A lamda + the definition of the pipeline and the topics is what we call a 
pipelet. Now to attach one pipelet to another we bolt them together.

|===||===||===||===||===||===||===||===||===||===||===||===||===|

##Demo
1. start your kafka cluster
2. run the groovy script
    * linux: `groovy scripty/random_normal.groovy`
    * windows: `groovy scripty\random_normal.groovy`
3. start listening on the topic
    * linux: `./kafka-console-kafkaConsumer.sh --bootstrap-server localhost:9092 --topic test111 --fro
m-beginning`
    * windows: `.\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test111 --fro
m-beginning`
4. now we can what we call from now on _bolt in a pipelet_
    * `curl -X PUT "http://localhost:8080/bolt/demo-pipeline?sourceTopic=test111&targetTopic=test222&lambda=http://localhost:8000/foo"`


## Ideas
* the pipelines should be visualized i.e. in [such a diagram](https://gojs.net/latest/samples/dynamicPorts.html)

## Demo
* We can unse an embedded kafka server for demo or testing purpose
* To realtime plot from a topic we can use [plotly dash](https://plot.ly/dash/).
  This is a python library and sadly the pyhton kafka client is not yet there for kafka 1.0.
  As a workaround there is a rest endpoint provided to poll on a topic
* We could fast and easily create python rest apis using [flask](https://blog.miguelgrinberg.com/post/designing-a-restful-api-with-python-and-flask)  
* there is a flask like api for groovy but sadly this has a [bug](https://github.com/perwendel/spark/issues/946#issuecomment-348493178)
