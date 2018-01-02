import React, { Component } from "react";
import { observer } from "mobx-react";
import { VictoryBar, VictoryChart, VictoryAxis } from 'victory';

@observer
class DemoPlot extends React.Component {
    componentDidMount() {
        this.props.store.poll();
    }

    render() {
        return(
            <div>
                {this.props.store.error}/
                {this.props.store.nextOffset}/
                {this.props.store.x.length}/
                {this.props.store.y.length}/{JSON.stringify(this.props.store.xy)}/
            
                <VictoryChart domainPadding={20} animate={{ duration: 300 }}>
                    <VictoryBar data={this.props.store.xy.slice()} 
                    x="o" 
                    y="y" />
                </VictoryChart>
            </div>
        );
    }
}

export default DemoPlot;
