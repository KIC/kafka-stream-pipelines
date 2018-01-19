import React, { Component } from "react";
import { observer } from "mobx-react";
import { observable } from "mobx";
import Plot from 'react-plotly.js';

@observer
class DemoPlot extends React.Component {
    
    componentDidMount() {
        
    }

    render() {
        return(
            <Plot data={[{ type: this.props.type, mode: this.props.mode, x: this.props.store.X(), y: this.props.store.Y() }]}
                layout={{ 
                    title: this.props.store.title + ' ' + this.props.type + ' Plot (' + this.props.store.length + ')'
                }}
            />
        );
    }
}

export default DemoPlot;
