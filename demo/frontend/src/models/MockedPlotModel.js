import { observable, computed, action } from "mobx";
import axios from 'axios';

export default class MockedPlotModel {
  @observable x = [];
  @observable y = [];
  @observable error = "";
  @observable nextOffset = 0;
  serviceUrl;
  pollDelay;
  
  constructor(serviceUrl, pollDelay) {
    this.serviceUrl = serviceUrl.replace(/\/+$/, '');
    this.pollDelay = pollDelay;
  }

  @action
  poll() {
    setTimeout((() => {
      this.x.push(this.x.length);
      this.y.push(Math.random());
      this.poll();
    }).bind(this), this.pollDelay)
  }

  @action.bound
  setError(error) {
    this.error = "" + error;
  }

  @action.bound
  addDataPoint(response) {
    if (response.data.keys.length > 0) {  
        response.data.offsets.forEach((x) => this.x.push(+x)); //keys.map(Number));
        response.data.values.forEach((y) => this.y.push(+y));
        
        for (var i=0; i<response.data.offsets.length; i++) 
            this.xy.push({o: response.data.offsets[i], x: +response.data.keys[i], y: +response.data.values[i]})
        
        this.nextOffset = response.data.nextOffset;
    }
  }
}
