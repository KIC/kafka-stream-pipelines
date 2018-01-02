import React, { Component } from "react";
import { observer } from "mobx-react";
import { observable } from "mobx";
import Plot from 'react-plotly.js';

@observer
class DemoPlot extends React.Component {
    @observable polling = true;

    componentDidMount() {
        this.props.store.poll();
    }

    render() {
        return(
            <div>
                <p>
                    <input type="checkbox" 
                           checked={this.props.store.polling} 
                           onChange={() => {this.props.store.polling = !this.props.store.polling;
                                            this.props.store.poll(); }} />
                    <span>polling</span>
                </p>
                <Plot data={[{ type: this.props.type, mode: this.props.mode, x: this.props.store.x.slice(), y: this.props.store.y.slice() }]}

                    layout={{ 
                        title: 'A Fancy ' + this.props.type + ' Plot (' + this.props.store.x.length + ')'
                    }}

                />

                { this.props.store.error ? <p> {this.props.store.error} </p> : null }
            </div>
        );
    }
}

export default DemoPlot;
