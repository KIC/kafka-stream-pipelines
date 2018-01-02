import { observable, computed, action } from "mobx";
import axios from 'axios';

export default class DemoPlotModel {
  @observable x = [];
  @observable y = [];
  @observable xy = [{o: -2, x: 0, y: 0.5}, {o: -1, x: 1, y: 0.2}];
  @observable error = "";
  @observable nextOffset = 0;
  @observable polling = true;
  serviceUrl;
  pollDelay;
  
  constructor(serviceUrl, pollDelay) {
    this.serviceUrl = serviceUrl.replace(/\/+$/, '');
    this.pollDelay = pollDelay;
  }

  @action
  poll() {
    if (this.polling) {
      axios.get(this.serviceUrl + "/" + this.nextOffset)
          .then(this.addDataPoint)
          .catch(this.setError)
          .then(() => setTimeout((() => {this.poll()}).bind(this), this.pollDelay));
    }
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
