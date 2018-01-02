import React, { Component } from "react";
import { observer } from "mobx-react";

import Task from "./Task";

@observer
class TaskList extends React.Component {
  
  componentDidMount() {
    this.props.store.poll();
  }

  render() {
    return (
      <ul>
        {this.props.store.tasks.map(task => (
          <Task task={task} />
        ))}
      </ul>
    );
  }

}

export default TaskList;
