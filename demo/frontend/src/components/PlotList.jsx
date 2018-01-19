import React, { Component } from "react";
import { observer } from "mobx-react";
import { Row, Col } from 'reactstrap';
import DemoPlot from "./DemoPlotlyPlot";

@observer
class PlotList extends React.Component {
  
  componentDidMount() {
    
  }

  render() {
    return (
        <Row>
            <Col>
                {this.props.store.map(plot => <DemoPlot type={plot.chartType} store={plot} />)}
            </Col>
        </Row>
    );
  }

}

export default PlotList;
