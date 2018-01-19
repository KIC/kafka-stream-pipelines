import React, { Component } from "react"
import { observer } from "mobx-react"
import { observable } from "mobx"
import { Container, Row, Col, Input, UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap'
import DevTools from "mobx-react-devtools"

import PlotList from "./PlotList"
import TaskList from "./TaskList"
import DemoPlot from "./DemoPlotlyPlot"
import SockJsClient from "./SocksJS"

@observer
class Main extends React.Component {
        
    @observable addPlotTopicInput = ""

    componentDidMount() {
    }

    render() {
        return (
            <Container>
                <SockJsClient url={this.props.sockJsURL} 
                            topics={this.props.store.topics.slice()}
                            onMessage={(topic, msg, headers) => this.props.store.onMessage(topic, msg, headers)}
                            sessionId={this.props.store.sessionId}/>
                
                <Row>
                    <Col xs="3">
                    {this.props.sockJsURL} via {this.props.store.sessionId}
                    </Col>
                    <Col xs="9">
                    Plot Topic:
                    <Input type="text" onChange={(e) => this.addPlotTopicInput = e.target.value}/>
                    <UncontrolledDropdown>
                        <DropdownToggle caret color="primary">Add Plot</DropdownToggle>
                        <DropdownMenu>
                        <DropdownItem onClick={() => this.props.store.subscribe("demo-pipeline", this.addPlotTopicInput, "bar")}>Bar</DropdownItem>
                        <DropdownItem onClick={() => this.props.store.subscribe("demo-pipeline", this.addPlotTopicInput, "lines")}>Line</DropdownItem>
                        </DropdownMenu>
                    </UncontrolledDropdown>
                    </Col>
                </Row>
                <Row>
                <Col xs="3">
                    <TaskList store={this.props.store.tasks} />
                </Col>
                <Col xs="9">
                    <PlotList store={this.props.store.plots} />
                </Col>
                </Row>
            </Container>
        );
  }

}

export default Main;
