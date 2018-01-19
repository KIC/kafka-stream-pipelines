import { observable, computed, action } from "mobx"

export default class PlotModel {
    @observable title = "" 
    @observable chartType = "bar"
    @observable title = ""  
    @observable x = []
    @observable y = []

    constructor(topic, chartType = "bar") {
        this.title = topic
        this.chartType = chartType
    }

    X() {
        return this.x.slice()
    }

    Y() {
        return this.y.slice()
    }

    @computed 
    get length() {
        return this.x.length - 1
    }
}