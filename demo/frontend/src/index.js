import React from "react"
import DevTools from "mobx-react-devtools"
import { render } from "react-dom"
import { Container } from 'reactstrap'

import Main from "./components/Main"
import DemoModel from "./models/DemoModel"

// Styling
import 'bootstrap/dist/css/bootstrap.min.css'
import styles from './index.css'

const store = new DemoModel(['/topic/tasks'])

render(
  <Container>
    <DevTools />
    <Main sockJsURL='http://localhost:8080/sockjs-node' store={store} />
  </Container>,
  document.getElementById("root")
);

// playing around in the console
window.store = store
