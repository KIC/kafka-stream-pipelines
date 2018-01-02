import React, { Component } from "react";
import { observer } from "mobx-react";
import classNames from 'classnames/bind';

/*
private String task;
private String lastException;
private String lastResult;
private boolean executing;
private int executionCount;
*/
const Task = observer(({ task }) => (
  <li class={classNames({executing: task.executing, failed: task.lastException})}>
    <span>{task.task}</span>
    <span>({task.executionCount})</span>
    <span>{task.lastException}</span>
  </li>
));

export default Task;
