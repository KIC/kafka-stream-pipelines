import React from "react";
import { render } from "react-dom";
import DevTools from "mobx-react-devtools";

// Components and Models
import TodoList from "./components/TodoList";
import TodoListModel from "./models/TodoListModel";
import TodoModel from "./models/TodoModel";

import TaskList from "./components/TaskList";
import DemoPlot from "./components/DemoPlotlyPlot"; //"./components/DemoVictoryPlot";
import DemoPlotMoldel from "./models/DemoPlotModel";
import DemoTasksModel from "./models/DemoTasksModel";
import MockedPlotModel from "./models/MockedPlotModel";

// Styling
import 'bootstrap/dist/css/bootstrap.min.css';
import styles from './index.css';
import { Container, Row, Col } from 'reactstrap';

const store = new TodoListModel();
const taskStore = new DemoTasksModel("/api/v1/tasks", 700);
const returnsPlotStore = new DemoPlotMoldel("/api/v1/poll/offset/demo-pipeline/demo.returns/", 1000);
const performancePlotStore = new DemoPlotMoldel("/api/v1/poll/offset/demo-pipeline/demo.performance/", 1000);
const mockReturnsPlotStore = new MockedPlotModel("/api/v1/poll/offset/demo-pipeline/demo.returns/", 1000);

render(
  <Container>
    <Row>
      <DevTools />
      <Col xs="3">
        <TaskList store={taskStore} />
      </Col>
      <Col xs="9">
        <Container>
          <Row>
            <Col>
              <DemoPlot type='bar' store={returnsPlotStore} />
            </Col>
          </Row>
          <Row>
            <Col>
              <DemoPlot type='lines' store={performancePlotStore} />
            </Col>
          </Row>
        </Container>
      </Col>
    </Row>
  </Container>,
  document.getElementById("root")
);

// playing around in the console
//window.store = store;
