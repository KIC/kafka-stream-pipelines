import { observable, computed, action } from "mobx"
import uuid from "../util/uuid"
import PlotModel from "./PlotModel"

export default class DemoModel {
    sessionId = uuid()
    @observable tasks = []
    @observable topics = []
    @observable plots = []

    constructor(initialTopics: Array = []) {
        this.topics = initialTopics
    }

  
    @action
    subscribe(pipelineId, topic, chartType) {
        this.plots.push(new PlotModel(topic, chartType))
        this.topics.push("/topic/" + pipelineId + "/" + topic + "/" + this.sessionId)
    }

    @action
    onMessage(topic, message, headers) {
        switch(topic) {
            case "/topic/tasks": 
                this.tasks = message
                break
            default: 
                this.addDataPoint(topic.split("/")[3], message.k, message.v)
        }
    }
  
    @action
    addDataPoint(target, x, y) {
        this.plots.filter(p => p.title == target)                
                  .forEach(p => {
                      p.x.push(x)
                      p.y.push(y)
                   })
    }

  }
  