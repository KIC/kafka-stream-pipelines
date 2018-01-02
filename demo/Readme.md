## Requirements
* groovy >= 2.5 (the demo service using sparkjava is not working with older groovy versions)
* java >= 1.8
* scala >= 2.11 

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
