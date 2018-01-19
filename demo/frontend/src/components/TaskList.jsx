import React, { Component } from "react";
import { observer } from "mobx-react";

import Task from "./Task";

@observer
class TaskList extends React.Component {
  
  componentDidMount() {
    
  }

  render() {
    return (
      <ul>
        {this.props.store.map(task => (
          <Task task={task} />
        ))}
      </ul>
    );
  }

}

export default TaskList;
