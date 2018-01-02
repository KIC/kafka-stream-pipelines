import { observable, computed, action } from "mobx";
import axios from 'axios';

export default class DemoTasksModel {
  @observable tasks = [];
  @observable error = "";
  serviceUrl;
  pollDelay;
  
  constructor(serviceUrl, pollDelay) {
    this.serviceUrl = serviceUrl.replace(/\/+$/, '');
    this.pollDelay = pollDelay;
  }

  @action
  poll() {
    axios.get(this.serviceUrl)
         .then(this.addTasks)
         .catch(this.setError)
         .then(() => setTimeout((() => {this.poll()}).bind(this), this.pollDelay));
  }

  @action.bound
  setError(error) {
    this.error = "" + error;
  }

  @action.bound
  addTasks(response) {
    this.tasks = response.data;
  }
}
